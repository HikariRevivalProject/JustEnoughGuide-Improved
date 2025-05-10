package me.eventually.jegimproved.integrations;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class AbstractIntegration {
    /**
     * Returns whether the integration is enabled or not.
     */
    public abstract boolean isEnabled();
    /**
     * Get the appendages that should be applied to the lore.
     */
    public abstract void getAppendages(Player p, SlimefunItem item, List<String> lore);
}
