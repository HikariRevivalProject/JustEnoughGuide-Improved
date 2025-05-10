package me.eventually.jegimproved.integrations;

import com.balugaq.jeg.implementation.JustEnoughGuide;
import com.balugaq.jeg.utils.ReflectionUtil;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class EMCTechIntegration extends AbstractIntegration {
    public static EMCTechIntegration integrationInstance;

    public static EMCTechIntegration getInstance() {
        if (integrationInstance == null) {
            integrationInstance = new EMCTechIntegration();
        }
        return integrationInstance;
    }
    /**
     * Returns whether the integration is enabled or not.
     */
    @Override
    public boolean isEnabled() {
        return JustEnoughGuide.getIntegrationManager().isEnabledEMCTech()
                && JustEnoughGuide.getConfigManager().enabledEMCIntegration();
    }

    /**
     * Get the appendages that should be applied to the lore.
     *
     * @param p
     * @param item
     * @param lore
     */
    @Override
    public void applyAppendages(Player p, SlimefunItem item, List<String> lore) {
        Class<?> emcCalculatorClass = null;
        try{
            emcCalculatorClass = Class.forName("io.github.sefiraat.emctech.emc.EmcCalculator");
        } catch (ClassNotFoundException e) {
            return;
        }
        Map<String, Double> emcMap = (Map<String, Double>) ReflectionUtil.getStaticValue(emcCalculatorClass, "SLIMEFUN_EMC_VALUES");
        String id = item.getId();
        if (emcMap.containsKey(id)) {
            lore.add("§7EMC: §b" + emcMap.get(id));
        }
    }
}
