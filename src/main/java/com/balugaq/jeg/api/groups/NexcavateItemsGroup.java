package com.balugaq.jeg.api.groups;

import city.norain.slimefun4.VaultIntegration;
import com.balugaq.jeg.api.interfaces.NotDisplayInSurvivalMode;
import com.balugaq.jeg.implementation.JustEnoughGuide;
import com.balugaq.jeg.utils.GuideUtil;
import com.balugaq.jeg.utils.ItemStackUtil;
import com.balugaq.jeg.utils.JEGVersionedItemFlag;
import com.balugaq.jeg.utils.compatibility.Converter;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.groups.FlexItemGroup;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.core.guide.GuideHistory;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuide;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideImplementation;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.chat.ChatInput;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.ItemUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * This class used to create groups to display all the marked items in the guide.
 * Displayed items are already marked in {@link ItemMarkGroup}
 * Players can't open this group if players haven't marked any item.
 *
 * @author balugaq
 * @since 1.1
 */
@SuppressWarnings({"deprecation", "unused"})
@NotDisplayInSurvivalMode
public class NexcavateItemsGroup extends FlexItemGroup {
    private static final List<SlimefunItem> ALL_SLIMEFUN_ITEMS = Slimefun.getRegistry().getAllSlimefunItems();
    private static final int BACK_SLOT = 1;
    private static final int SEARCH_SLOT = 7;
    private static final int PREVIOUS_SLOT = 46;
    private static final int NEXT_SLOT = 52;
    private static final int[] BORDER = new int[]{0, 2, 3, 4, 5, 6, 8, 45, 47, 48, 49, 50, 51, 53};
    private static final int[] MAIN_CONTENT = new int[]{
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    };

    private static final JavaPlugin JAVA_PLUGIN = JustEnoughGuide.getInstance();
    private final int page;
    private final List<SlimefunItem> slimefunItemList;
    private Map<Integer, NexcavateItemsGroup> pageMap = new LinkedHashMap<>();

    @ParametersAreNonnullByDefault
    public NexcavateItemsGroup(NamespacedKey key, ItemStack icon) {
        super(key, icon);
        this.page = 1;
        List<SlimefunItem> slimefunItemList = new ArrayList<>();
        for (SlimefunItem item : ALL_SLIMEFUN_ITEMS) {
            if ("nexcavate".equalsIgnoreCase(item.getAddon().getName())) {
                slimefunItemList.add(item);
            }
        }
        this.slimefunItemList = slimefunItemList;
        this.pageMap.put(1, this);
    }

    /**
     * Constructor of nexvacateItemsGroup.
     *
     * @param nexcavateItemsGroup The nexvacateItemsGroup to copy.
     * @param page                The page number to display.
     */
    protected NexcavateItemsGroup(@NotNull NexcavateItemsGroup nexcavateItemsGroup, int page) {
        super(nexcavateItemsGroup.key, new ItemStack(Material.BARRIER));
        this.page = page;
        this.slimefunItemList = nexcavateItemsGroup.slimefunItemList;
        this.pageMap.put(page, this);
    }

    /**
     * Always returns false.
     *
     * @param player            The player who opened the group.
     * @param playerProfile     The player's profile.
     * @param slimefunGuideMode The Slimefun guide mode.
     * @return false.
     */
    @Override
    public boolean isVisible(
            @NotNull Player player,
            @NotNull PlayerProfile playerProfile,
            @NotNull SlimefunGuideMode slimefunGuideMode) {
        return true;
    }

    /**
     * Opens the group for the player.
     *
     * @param player            The player who opened the group.
     * @param playerProfile     The player's profile.
     * @param slimefunGuideMode The Slimefun guide mode.
     */
    @Override
    public void open(
            @NotNull Player player,
            @NotNull PlayerProfile playerProfile,
            @NotNull SlimefunGuideMode slimefunGuideMode) {
        playerProfile.getGuideHistory().add(this, this.page);
        this.generateMenu(player, playerProfile, slimefunGuideMode).open(player);
    }

    /**
     * Reopens the menu for the player.
     *
     * @param player            The player who opened the group.
     * @param playerProfile     The player's profile.
     * @param slimefunGuideMode The Slimefun guide mode.
     */
    public void refresh(
            @NotNull Player player,
            @NotNull PlayerProfile playerProfile,
            @NotNull SlimefunGuideMode slimefunGuideMode) {
        GuideUtil.removeLastEntry(playerProfile.getGuideHistory());
        this.open(player, playerProfile, slimefunGuideMode);
    }

    /**
     * Generates the menu for the player.
     *
     * @param player            The player who opened the group.
     * @param playerProfile     The player's profile.
     * @param slimefunGuideMode The Slimefun guide mode.
     * @return The generated menu.
     */
    @NotNull
    private ChestMenu generateMenu(
            @NotNull Player player,
            @NotNull PlayerProfile playerProfile,
            @NotNull SlimefunGuideMode slimefunGuideMode) {
        ChestMenu chestMenu = new ChestMenu("隐藏物品");

        chestMenu.setEmptySlotsClickable(false);
        chestMenu.addMenuOpeningHandler(pl -> pl.playSound(pl.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1));

        chestMenu.addItem(BACK_SLOT, ItemStackUtil.getCleanItem(ChestMenuUtils.getBackButton(player)));
        chestMenu.addMenuClickHandler(BACK_SLOT, (pl, s, is, action) -> {
            GuideHistory guideHistory = playerProfile.getGuideHistory();
            if (action.isShiftClicked()) {
                SlimefunGuide.openMainMenu(playerProfile, slimefunGuideMode, guideHistory.getMainMenuPage());
            } else {
                guideHistory.goBack(Slimefun.getRegistry().getSlimefunGuide(SlimefunGuideMode.CHEAT_MODE));
            }
            return false;
        });

        SlimefunGuideImplementation implementation = Slimefun.getRegistry().getSlimefunGuide(SlimefunGuideMode.CHEAT_MODE);

        // Search feature!
        chestMenu.addItem(SEARCH_SLOT, ItemStackUtil.getCleanItem(ChestMenuUtils.getSearchButton(player)));
        chestMenu.addMenuClickHandler(SEARCH_SLOT, (pl, slot, item, action) -> {
            pl.closeInventory();

            Slimefun.getLocalization().sendMessage(pl, "guide.search.message");
            ChatInput.waitForPlayer(
                    JAVA_PLUGIN,
                    pl,
                    msg -> implementation.openSearch(
                            playerProfile, msg, implementation.getMode() == SlimefunGuideMode.SURVIVAL_MODE));

            return false;
        });

        chestMenu.addItem(
                PREVIOUS_SLOT,
                ItemStackUtil.getCleanItem(ChestMenuUtils.getPreviousButton(
                        player, this.page, (this.slimefunItemList.size() - 1) / MAIN_CONTENT.length + 1)));
        chestMenu.addMenuClickHandler(PREVIOUS_SLOT, (p, slot, item, action) -> {
            GuideUtil.removeLastEntry(playerProfile.getGuideHistory());
            NexcavateItemsGroup nexcavateItemsGroup = this.getByPage(Math.max(this.page - 1, 1));
            nexcavateItemsGroup.open(player, playerProfile, slimefunGuideMode);
            return false;
        });

        chestMenu.addItem(
                NEXT_SLOT,
                ItemStackUtil.getCleanItem(ChestMenuUtils.getNextButton(
                        player, this.page, (this.slimefunItemList.size() - 1) / MAIN_CONTENT.length + 1)));
        chestMenu.addMenuClickHandler(NEXT_SLOT, (p, slot, item, action) -> {
            GuideUtil.removeLastEntry(playerProfile.getGuideHistory());
            NexcavateItemsGroup nexcavateItemsGroup = this.getByPage(
                    Math.min(this.page + 1, (this.slimefunItemList.size() - 1) / MAIN_CONTENT.length + 1));
            nexcavateItemsGroup.open(player, playerProfile, slimefunGuideMode);
            return false;
        });

        for (int slot : BORDER) {
            chestMenu.addItem(slot, ItemStackUtil.getCleanItem(ChestMenuUtils.getBackground()));
            chestMenu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
        }

        for (int i = 0; i < MAIN_CONTENT.length; i++) {
            int index = i + this.page * MAIN_CONTENT.length - MAIN_CONTENT.length;
            if (index < this.slimefunItemList.size()) {
                SlimefunItem slimefunItem = slimefunItemList.get(index);
                Research research = slimefunItem.getResearch();
                ItemStack itemstack;
                ChestMenu.MenuClickHandler handler;
                if (implementation.getMode() == SlimefunGuideMode.SURVIVAL_MODE
                        && research != null
                        && !playerProfile.hasUnlocked(research)) {
                    String lore;

                    if (VaultIntegration.isEnabled()) {
                        lore = String.format("%.2f", research.getCurrencyCost()) + " 游戏币";
                    } else {
                        lore = research.getLevelCost() + " 级经验";
                    }

                    itemstack = ItemStackUtil.getCleanItem(Converter.getItem(
                            ChestMenuUtils.getNoPermissionItem(),
                            "&f" + ItemUtils.getItemName(slimefunItem.getItem()),
                            "&7" + slimefunItem.getId(),
                            "&4&l" + Slimefun.getLocalization().getMessage(player, "guide.locked"),
                            "",
                            "&a> 单击解锁",
                            "",
                            "&7需要 &b",
                            lore));
                    handler = (pl, slot, item, action) -> {
                        research.unlockFromGuide(
                                implementation, pl, playerProfile, slimefunItem, slimefunItem.getItemGroup(), page);
                        return false;
                    };
                } else {
                    itemstack = ItemStackUtil.getCleanItem(Converter.getItem(slimefunItem.getItem(), meta -> {
                        ItemGroup itemGroup = slimefunItem.getItemGroup();
                        if (meta.hasLore() && meta.getLore() != null) {
                            List<String> lore = meta.getLore();
                            meta.setLore(lore);
                        }

                        meta.addItemFlags(
                                ItemFlag.HIDE_ATTRIBUTES,
                                ItemFlag.HIDE_ENCHANTS,
                                JEGVersionedItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                    }));
                    handler = (pl, slot, itm, action) -> {
                        try {
                            if (implementation.getMode() != SlimefunGuideMode.SURVIVAL_MODE
                                    && (pl.isOp() || pl.hasPermission("slimefun.cheat.items"))) {
                                pl.getInventory()
                                        .addItem(slimefunItem.getItem().clone());
                            } else {
                                implementation.displayItem(playerProfile, slimefunItem, true);
                            }
                        } catch (Exception | LinkageError x) {
                            printErrorMessage(pl, slimefunItem, x);
                        }

                        return false;
                    };
                }

                chestMenu.addItem(MAIN_CONTENT[i], ItemStackUtil.getCleanItem(itemstack), handler);
            }
        }

        chestMenu.addItem(48, ItemStackUtil.getCleanItem(GuideUtil.getItemMarkMenuButton()));
        chestMenu.addMenuClickHandler(48, (pl, s, is, action) -> {
            GuideHistory guideHistory = playerProfile.getGuideHistory();
            if (action.isShiftClicked()) {
                SlimefunGuide.openMainMenu(playerProfile, slimefunGuideMode, guideHistory.getMainMenuPage());
            } else {
                guideHistory.goBack(Slimefun.getRegistry().getSlimefunGuide(SlimefunGuideMode.SURVIVAL_MODE));
            }
            return false;
        });

        chestMenu.addItem(49, ItemStackUtil.getCleanItem(GuideUtil.getBookMarkMenuButton()));
        chestMenu.addMenuClickHandler(49, (pl, s, is, action) -> {
            GuideHistory guideHistory = playerProfile.getGuideHistory();
            if (action.isShiftClicked()) {
                SlimefunGuide.openMainMenu(playerProfile, slimefunGuideMode, guideHistory.getMainMenuPage());
            } else {
                guideHistory.goBack(Slimefun.getRegistry().getSlimefunGuide(SlimefunGuideMode.SURVIVAL_MODE));
            }
            return false;
        });

        return chestMenu;
    }

    /**
     * Gets the nexvacateItemsGroup by page.
     *
     * @param page The page number.
     * @return The nexvacateItemsGroup by page.
     */
    @NotNull
    private NexcavateItemsGroup getByPage(int page) {
        if (this.pageMap.containsKey(page)) {
            return this.pageMap.get(page);
        } else {
            synchronized (this.pageMap.get(1)) {
                if (this.pageMap.containsKey(page)) {
                    return this.pageMap.get(page);
                }

                NexcavateItemsGroup nexcavateItemsGroup = new NexcavateItemsGroup(this, page);
                nexcavateItemsGroup.pageMap = this.pageMap;
                this.pageMap.put(page, nexcavateItemsGroup);
                return nexcavateItemsGroup;
            }
        }
    }

    /**
     * Checks if the item group is accessible for the player.
     *
     * @param p            The player.
     * @param slimefunItem The Slimefun item.
     * @return True if the item group is accessible for the player.
     */
    @ParametersAreNonnullByDefault
    private boolean isItemGroupAccessible(@NotNull Player p, @NotNull SlimefunItem slimefunItem) {
        return Slimefun.getConfigManager().isShowHiddenItemGroupsInSearch()
                || slimefunItem.getItemGroup().isAccessible(p);
    }

    /**
     * Prints an error message to the player.
     *
     * @param p The player.
     * @param x The exception.
     */
    @ParametersAreNonnullByDefault
    private void printErrorMessage(@NotNull Player p, @NotNull Throwable x) {
        p.sendMessage("&4服务器发生了一个内部错误. 请联系管理员处理.");
        JAVA_PLUGIN.getLogger().log(Level.SEVERE, "在打开指南书里的 Slimefun 物品时发生了意外!", x);
    }

    /**
     * Prints an error message to the player.
     *
     * @param p    The player.
     * @param item The Slimefun item.
     * @param x    The exception.
     */
    @ParametersAreNonnullByDefault
    private void printErrorMessage(@NotNull Player p, @NotNull SlimefunItem item, @NotNull Throwable x) {
        p.sendMessage(ChatColor.DARK_RED
                + "An internal server error has occurred. Please inform an admin, check the console for"
                + " further info.");
        item.error(
                "This item has caused an error message to be thrown while viewing it in the Slimefun" + " guide.", x);
    }

    @Override
    public boolean isCrossAddonItemGroup() {
        return true;
    }

    @Override
    public int getTier() {
        return Integer.MAX_VALUE;
    }
}
