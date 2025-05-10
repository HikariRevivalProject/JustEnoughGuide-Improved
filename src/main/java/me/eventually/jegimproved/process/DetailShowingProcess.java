package me.eventually.jegimproved.process;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import me.eventually.jegimproved.integrations.EMCTechIntegration;
import me.eventually.jegimproved.integrations.FinalTechChangedIntegration;
import me.eventually.jegimproved.integrations.RykenSlimefunCustomizerIntegration;
import org.bukkit.entity.Player;

import java.util.List;

public class DetailShowingProcess {
    public static void appendDetail(PlayerProfile profile, SlimefunItem sfItem, List<String> lore) {
        RykenSlimefunCustomizerIntegration rykenSlimefunCustomizerIntegration = RykenSlimefunCustomizerIntegration.getInstance();
        Player p = profile.getPlayer();
        if (rykenSlimefunCustomizerIntegration.isEnabled()) {
            rykenSlimefunCustomizerIntegration.applyAppendages(p, sfItem, lore);
        }
        EMCTechIntegration emcTechIntegration = EMCTechIntegration.getInstance();
        if (emcTechIntegration.isEnabled()) {
            emcTechIntegration.applyAppendages(p, sfItem, lore);
        }
        FinalTechChangedIntegration finalTechChangedIntegration = FinalTechChangedIntegration.getInstance();
        if (finalTechChangedIntegration.isEnabled()) {
            finalTechChangedIntegration.applyAppendages(p, sfItem, lore);
        }
    }
}