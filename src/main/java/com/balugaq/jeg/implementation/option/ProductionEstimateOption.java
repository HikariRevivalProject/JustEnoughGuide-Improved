package com.balugaq.jeg.implementation.option;

import com.balugaq.jeg.implementation.JustEnoughGuide;
import com.balugaq.jeg.implementation.guide.SurvivalGuideImplementation;
import com.balugaq.jeg.utils.compatibility.Converter;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.core.guide.options.SlimefunGuideOption;
import io.github.thebusybiscuit.slimefun4.core.guide.options.SlimefunGuideSettings;
import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ProductionEstimateOption implements SlimefunGuideOption<Integer> {
    private static ProductionEstimateOption optionInstance;
    public static ProductionEstimateOption getInstance() {
        if (optionInstance == null) {
            optionInstance = new ProductionEstimateOption();
        }
        return optionInstance;
    }

    public int getIndex(Player p) {
        return PersistentDataAPI.getInt(p, getKey());
    }

    @Override
    public @NotNull SlimefunAddon getAddon() {
        return JustEnoughGuide.getInstance();
    }

    @Override
    public Optional<ItemStack> getDisplayItem(Player p, ItemStack guide) {
        int value = getSelectedOption(p, guide).orElse(0);
        int next = value == 2 ? 0 : value + 1;
        ItemStack item = Converter.getItem(
                Material.CLOCK,
                "&b产量预测单位: &6&l" + SurvivalGuideImplementation.timeDurations.get(value).getFirstValue(),
                "&7你可以选择产量预测的单位.",
                "&7例如: 1 分钟, 1 小时, 1 天.",
                "",
                "&7\u21E8 &e点击 选择 " + SurvivalGuideImplementation.timeDurations.get(next).getFirstValue() + " 作为单位"
        );
        return Optional.of(item);
    }

    @Override
    public void onClick(Player p, ItemStack guide) {
        int value = getSelectedOption(p, guide).orElse(-1);
        value++;
        if (value > 2) {
            value = 0;
        }
        setSelectedOption(p, guide, value);
    }

    @Override
    public Optional<Integer> getSelectedOption(Player p, ItemStack guide) {
        NamespacedKey key = getKey();
        Integer value = PersistentDataAPI.hasInt(p, key) ? PersistentDataAPI.getInt(p, key) : 0;
        return Optional.of(value);
    }

    @Override
    public void setSelectedOption(Player p, ItemStack guide, Integer value) {
        PersistentDataAPI.setInt(p, getKey(), value);
        SlimefunGuideSettings.openSettings(p, guide);
    }

    /**
     * Return the namespaced identifier for this object.
     *
     * @return this object's key
     */
    @Override
    public @NotNull NamespacedKey getKey() {
        return new NamespacedKey(JustEnoughGuide.getInstance(), "production_estimate_time");
    }

}
