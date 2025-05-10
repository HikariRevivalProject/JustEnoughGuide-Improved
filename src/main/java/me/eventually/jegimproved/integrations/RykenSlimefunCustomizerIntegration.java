package me.eventually.jegimproved.integrations;

import com.balugaq.jeg.implementation.JustEnoughGuide;
import com.balugaq.jeg.implementation.guide.SurvivalGuideImplementation;
import com.balugaq.jeg.implementation.option.ProductionEstimateOption;
import com.balugaq.jeg.utils.ReflectionUtil;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import net.guizhanss.guizhanlib.minecraft.helper.inventory.ItemStackHelper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RykenSlimefunCustomizerIntegration extends AbstractIntegration {

    public static RykenSlimefunCustomizerIntegration integrationInstance;

    public static RykenSlimefunCustomizerIntegration getInstance() {
        if (integrationInstance == null) {
            integrationInstance = new RykenSlimefunCustomizerIntegration();
        }
        return integrationInstance;
    }

    /**
     * Returns whether the integration is enabled or not.
     */
    @Override
    public boolean isEnabled() {
        return JustEnoughGuide.getIntegrationManager().isEnabledRykenSlimefunCustomizer()
                && JustEnoughGuide.getConfigManager().enabledRSCIntegration();
    }

    /**
     * Get the appendages that should be applied to the lore.
     */
    @Override
    public void applyAppendages(Player p, SlimefunItem item, List<String> lore) {
        Class<?> clazz = item.getClass();
        boolean isRSCGenerator = (
                clazz.getSimpleName().equals("CustomMaterialGenerator")
            && clazz.getPackageName().contains("rykenslimefuncustomizer")
        );

        if (isRSCGenerator) {
            String tickerProfilerTimeWithSuffix = Slimefun.getProfiler().getTime();
            String tickerProfilerTime = tickerProfilerTimeWithSuffix.substring(0, tickerProfilerTimeWithSuffix.length() - 2);
            float tickerTimeFloat = Math.max(Float.parseFloat(tickerProfilerTime), 50.0f * Slimefun.getProfiler().getTickRate());
            String estimateUnit = SurvivalGuideImplementation.timeDurations.get(ProductionEstimateOption.getInstance().getIndex(p)).getFirstValue();
            long estimateTime = SurvivalGuideImplementation.timeDurations.get(ProductionEstimateOption.getInstance().getIndex(p)).getSecondValue().longValue();
            Long estimateTicks = (long) (estimateTime / tickerTimeFloat);

            boolean chooseOne = (boolean) ReflectionUtil.getValue(item, "chooseOne");
            if (chooseOne) return;

            int tickRate = (int) ReflectionUtil.getValue(item, "tickRate");
            Long estimateGenerations = estimateTicks / tickRate;
            List<ItemStack> generationList = (List<ItemStack>) ReflectionUtil.getValue(item, "generation");
            if (generationList == null || generationList.isEmpty()) return;

            lore.add("§7刻时间" + tickerTimeFloat + "ms, 预计每" + estimateUnit + "生成" + estimateGenerations + "次物品");
            for (ItemStack generation : generationList){
                String itemName = ItemStackHelper.getDisplayName(generation);
                long amount = generation.getAmount() * estimateGenerations;
                lore.add("§d每" + estimateUnit + " §7生成 §d" + itemName + " §7" + amount + " 个");
            }
        }
    }
}
