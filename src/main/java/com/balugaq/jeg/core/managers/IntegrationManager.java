package com.balugaq.jeg.core.managers;

import com.balugaq.jeg.api.managers.AbstractManager;
import com.balugaq.jeg.implementation.JustEnoughGuide;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * This class is responsible for managing integrations with other plugins.
 *
 * @author balugaq
 * @since 1.2
 */
@Getter
public class IntegrationManager extends AbstractManager {
    private final @NotNull JavaPlugin plugin;
    private final boolean enabledNetworksExpansion;
    private final boolean enabledOreWorkshop;
    private final boolean enabledEMCTech;
    private final boolean enabledRykenSlimefunCustomizer;
    private final boolean enabledFinalTechChanged;

    public IntegrationManager(@NotNull JavaPlugin plugin) {
        boolean tmp;
        this.plugin = plugin;

        // Check if NetworksExpansion is enabled
        try {
            Class.forName("com.ytdd9527.networksexpansion.core.listener.NetworksGuideListener");
            tmp = true;
        } catch (ClassNotFoundException e) {
            tmp = false;
        }

        enabledNetworksExpansion = tmp;

        // Integrations
        // Original part
        this.enabledOreWorkshop = isEnabled("OreWorkshop");
        // Fork part
        this.enabledRykenSlimefunCustomizer = isEnabled("RykenSlimefunCustomizer");
        this.enabledEMCTech = isEnabled("EMCTech");
        this.enabledFinalTechChanged = isEnabled("FinalTECH-Changed");
    }

    private static boolean isEnabled(String pluginName) {
        return JustEnoughGuide.getInstance().getServer().getPluginManager().isPluginEnabled(pluginName);
    }
}
