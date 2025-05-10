package com.balugaq.jeg.implementation;

import com.balugaq.jeg.core.managers.*;
import com.balugaq.jeg.implementation.guide.CheatGuideImplementation;
import com.balugaq.jeg.implementation.guide.SurvivalGuideImplementation;
import com.balugaq.jeg.implementation.items.GroupSetup;
import com.balugaq.jeg.implementation.option.BeginnersGuideOption;
import com.balugaq.jeg.implementation.option.ProductionEstimateOption;
import com.balugaq.jeg.utils.MinecraftVersion;
import com.balugaq.jeg.utils.ReflectionUtil;
import com.balugaq.jeg.utils.UUIDUtils;
import com.google.common.base.Preconditions;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideImplementation;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import io.github.thebusybiscuit.slimefun4.core.guide.options.SlimefunGuideSettings;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.guide.CheatSheetSlimefunGuide;
import io.github.thebusybiscuit.slimefun4.implementation.guide.SurvivalSlimefunGuide;
import io.github.thebusybiscuit.slimefun4.utils.NumberUtils;
import lombok.Getter;
import net.guizhanss.guizhanlibplugin.updater.GuizhanUpdater;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * This is the main class of the JustEnoughGuide plugin.
 * It depends on the Slimefun4 plugin and provides a set of features to improve the game experience.
 *
 * @author balugaq
 * @since 1.0
 */
@SuppressWarnings("unused")
@Getter
public class JustEnoughGuide extends JavaPlugin implements SlimefunAddon {
    public static final int RECOMMENDED_JAVA_VERSION = 17;
    public static final MinecraftVersion RECOMMENDED_MC_VERSION = MinecraftVersion.MINECRAFT_1_16;
    @Getter
    private static JustEnoughGuide instance;
    @Getter
    private static UUID serverUUID;
    @Getter
    private final @NotNull String username;
    @Getter
    private final @NotNull String repo;
    @Getter
    private final @NotNull String branch;
    @Getter
    private BookmarkManager bookmarkManager;
    @Getter
    private CommandManager commandManager;
    @Getter
    private ConfigManager configManager;
    @Getter
    private IntegrationManager integrationManager;
    @Getter
    private ListenerManager listenerManager;
    @Getter
    private RTSBackpackManager rtsBackpackManager;
    @Getter
    private MinecraftVersion minecraftVersion;
    @Getter
    private int javaVersion;

    public JustEnoughGuide() {
        this.username = "balugaq";
        this.repo = "JustEnoughGuide";
        this.branch = "master";
    }

    public static BookmarkManager getBookmarkManager() {
        return getInstance().bookmarkManager;
    }

    public static CommandManager getCommandManager() {
        return getInstance().commandManager;
    }

    public static ConfigManager getConfigManager() {
        return getInstance().configManager;
    }

    public static IntegrationManager getIntegrationManager() {
        return getInstance().integrationManager;
    }

    public static ListenerManager getListenerManager() {
        return getInstance().listenerManager;
    }

    public static MinecraftVersion getMCVersion() {
        return getInstance().minecraftVersion;
    }

    public static @NotNull JustEnoughGuide getInstance() {
        Preconditions.checkArgument(instance != null, "JustEnoughGuide 未被启用！");
        return JustEnoughGuide.instance;
    }

    /**
     * Initializes the plugin and sets up all necessary components.
     */
    @Override
    public void onEnable() {
        Preconditions.checkArgument(instance == null, "JustEnoughGuide 已被启用！");
        instance = this;

        getLogger().info("正在加载配置文件...");
        saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        this.configManager.load();

        // Checking environment compatibility
        boolean isCompatible = environmentCheck();

        if (!isCompatible) {
            getLogger().warning("环境不兼容！插件已被禁用！");
            onDisable();
            return;
        }

        getLogger().info("正在适配其他插件...");
        this.integrationManager = new IntegrationManager(this);
        this.integrationManager.load();

        getLogger().info("正在注册监听器...");
        this.listenerManager = new ListenerManager(this);
        this.listenerManager.load();

        getLogger().info("尝试自动更新...");
        tryUpdate();

        getLogger().info("正在注册指令");
        this.commandManager = new CommandManager(this);
        this.commandManager.load();

        if (!commandManager.registerCommands()) {
            getLogger().warning("注册指令失败！");
        }

        final boolean survivalOverride = getConfigManager().isSurvivalImprovement();
        final boolean cheatOverride = getConfigManager().isCheatImprovement();
        if (survivalOverride || cheatOverride) {
            getLogger().info("已开启指南替换！");
            getLogger().info("正在替换指南...");
            Field field = ReflectionUtil.getField(Slimefun.getRegistry().getClass(), "guides");
            if (field != null) {
                field.setAccessible(true);

                Map<SlimefunGuideMode, SlimefunGuideImplementation> newGuides = new EnumMap<>(SlimefunGuideMode.class);
                newGuides.put(
                        SlimefunGuideMode.SURVIVAL_MODE,
                        survivalOverride ? new SurvivalGuideImplementation() : new SurvivalSlimefunGuide());
                newGuides.put(
                        SlimefunGuideMode.CHEAT_MODE,
                        cheatOverride ? new CheatGuideImplementation() : new CheatSheetSlimefunGuide());
                try {
                    field.set(Slimefun.getRegistry(), newGuides);
                } catch (IllegalAccessException ignored) {

                }
            }
            getLogger().info(survivalOverride ? "已开启替换生存指南" : "未开启替换生存指南");
            getLogger().info(cheatOverride ? "已开启替换作弊指南" : "未开启替换作弊指南");

            getLogger().info("正在加载书签...");
            this.bookmarkManager = new BookmarkManager(this);
            this.bookmarkManager.load();

            getLogger().info("正在加载教学物品组...");
            GroupSetup.setup();
            getLogger().info("教学物品组加载完毕！");

            if (getConfigManager().isBeginnerOption()) {
                getLogger().info("正在加载新手指南选项...");
                SlimefunGuideSettings.addOption(new BeginnersGuideOption());
                getLogger().info("新手指南选项加载完毕！");
            }
            if (getConfigManager().enabledRSCIntegration()) {
                getLogger().info("正在加载产量预测选项...");
                SlimefunGuideSettings.addOption(ProductionEstimateOption.getInstance());
                getLogger().info("产量预测选项加载完毕！");
            }
        }

        this.rtsBackpackManager = new RTSBackpackManager(this);
        this.rtsBackpackManager.load();

        File uuidFile = new File(getDataFolder(), "server-uuid");
        if (uuidFile.exists()) {
            try {
                serverUUID = UUID.nameUUIDFromBytes(Files.readAllBytes(Path.of(uuidFile.getPath())));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            serverUUID = UUID.randomUUID();
            try {
                getDataFolder().mkdirs();
                uuidFile.createNewFile();
                Files.write(Path.of(uuidFile.getPath()), UUIDUtils.toByteArray(serverUUID));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        getLogger().info("成功启用此附属");
    }

    /**
     * Attempts to update the plugin if auto-update is enabled.
     */
    public void tryUpdate() {
        try {
            if (configManager.isAutoUpdate() && getDescription().getVersion().startsWith("Build")) {
                GuizhanUpdater.start(this, getFile(), username, repo, branch);
            }
        } catch (NoClassDefFoundError | NullPointerException | UnsupportedClassVersionError e) {
            getLogger().info("自动更新失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cleans up resources and shuts down the plugin.
     */
    @Override
    public void onDisable() {
        Preconditions.checkArgument(instance != null, "JustEnoughGuide 未被启用！");
        GroupSetup.shutdown();

        Field field = ReflectionUtil.getField(Slimefun.getRegistry().getClass(), "guides");
        if (field != null) {
            field.setAccessible(true);

            Map<SlimefunGuideMode, SlimefunGuideImplementation> newGuides = new EnumMap<>(SlimefunGuideMode.class);
            newGuides.put(SlimefunGuideMode.SURVIVAL_MODE, new SurvivalSlimefunGuide());
            newGuides.put(SlimefunGuideMode.CHEAT_MODE, new CheatSheetSlimefunGuide());
            try {
                field.set(Slimefun.getRegistry(), newGuides);
            } catch (IllegalAccessException ignored) {

            }
        }

        // Managers
        if (this.bookmarkManager != null) {
            this.bookmarkManager.unload();
        }

        if (this.integrationManager != null) {
            this.integrationManager.unload();
        }

        if (this.commandManager != null) {
            this.commandManager.unload();
        }

        if (this.listenerManager != null) {
            this.listenerManager.unload();
        }

        if (this.rtsBackpackManager != null) {
            this.rtsBackpackManager.unload();
        }

        if (this.configManager != null) {
            this.configManager.unload();
        }

        this.bookmarkManager = null;
        this.integrationManager = null;
        this.commandManager = null;
        this.listenerManager = null;
        this.configManager = null;

        // Other fields
        this.minecraftVersion = null;
        this.javaVersion = 0;

        // Clear instance
        instance = null;
        getLogger().info("成功禁用此附属");
    }

    /**
     * Returns the JavaPlugin instance.
     *
     * @return the JavaPlugin instance
     */
    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    /**
     * Returns the bug tracker URL for the plugin.
     *
     * @return the bug tracker URL
     */
    @Nullable
    @Override
    public String getBugTrackerURL() {
        return MessageFormat.format("https://github.com/{0}/{1}/issues/", this.username, this.repo);
    }

    /**
     * Logs a debug message if debugging is enabled.
     *
     * @param message the debug message to log
     */
    public void debug(String message) {
        if (getConfigManager().isDebug()) {
            getLogger().warning("[DEBUG] " + message);
        }
    }

    /**
     * Returns the version of the plugin.
     *
     * @return the version of the plugin
     */
    public @NotNull String getVersion() {
        return getDescription().getVersion();
    }

    /**
     * Checks the environment compatibility for the plugin.
     *
     * @return true if the environment is compatible, false otherwise
     */
    private boolean environmentCheck() {
        this.minecraftVersion = MinecraftVersion.getCurrentVersion();
        this.javaVersion = NumberUtils.getJavaVersion();
        if (minecraftVersion == null) {
            getLogger().warning("无法获取到 Minecraft 版本！");
            return false;
        }

        if (minecraftVersion == MinecraftVersion.UNKNOWN) {
            getLogger().warning("无法识别到 Minecraft 版本！");
        }

        if (!minecraftVersion.isAtLeast(RECOMMENDED_MC_VERSION)) {
            getLogger().warning("Minecraft 版本过低，请使用 Minecraft 1." + RECOMMENDED_MC_VERSION.getMajor() + "." + RECOMMENDED_MC_VERSION.getMinor() + " 或以上版本！");
        }

        if (javaVersion < RECOMMENDED_JAVA_VERSION) {
            getLogger().warning("Java 版本过低，请使用 Java " + RECOMMENDED_JAVA_VERSION + " 或以上版本！");
        }

        return true;
    }

    /**
     * Checks if debugging is enabled.
     *
     * @return true if debugging is enabled, false otherwise
     */
    public boolean isDebug() {
        return getConfigManager().isDebug();
    }
}
