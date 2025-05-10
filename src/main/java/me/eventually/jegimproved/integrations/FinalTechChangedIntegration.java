package me.eventually.jegimproved.integrations;

import com.balugaq.jeg.implementation.JustEnoughGuide;
import com.balugaq.jeg.utils.ReflectionUtil;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class FinalTechChangedIntegration extends AbstractIntegration {
    public static FinalTechChangedIntegration integrationInstance;

    public static FinalTechChangedIntegration getInstance() {
        if (integrationInstance == null) {
            integrationInstance = new FinalTechChangedIntegration();
        }
        return integrationInstance;
    }

    /**
     * Returns whether the integration is enabled or not.
     */
    @Override
    public boolean isEnabled() {
        return JustEnoughGuide.getIntegrationManager().isEnabledFinalTechChanged()
                && JustEnoughGuide.getConfigManager().enabledFTCIntegration();
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
        Class<?> valueTableClass;
        try {
            valueTableClass = Class.forName("io.taraxacum.libs.slimefun.dto.ItemValueTable");
        } catch (ClassNotFoundException e) {
            return;
        }
        Method getInstanceMethod = ReflectionUtil.getMethod(valueTableClass, "getInstance");

        Object itemValueTableClassInstance;
        try {
            itemValueTableClassInstance = getInstanceMethod.invoke(null);
        } catch (InvocationTargetException | IllegalAccessException e) {
            return;
        }
        Method getOrCalItemInputValue = ReflectionUtil.getMethod(valueTableClass, "getOrCalItemInputValue", ItemStack.class);
        Method getOrCalItemOutputValue = ReflectionUtil.getMethod(valueTableClass, "getOrCalItemOutputValue", ItemStack.class);
        Validate.notNull(getOrCalItemInputValue);
        Validate.notNull(getOrCalItemOutputValue);
        String itemOutputValue;
        String itemInputValue;
        try {
            itemInputValue = (String) getOrCalItemInputValue.invoke(itemValueTableClassInstance, item.getItem());
            itemOutputValue = (String) getOrCalItemOutputValue.invoke(itemValueTableClassInstance, item.getItem());
        } catch (IllegalAccessException | InvocationTargetException e) {
            return;
        }
        lore.add("§7乱序输入价值:" + itemInputValue);
        if (!itemOutputValue.equals("INFINITY")) {
            lore.add("§7乱序输出价值:" + itemOutputValue);
        }
    }
}
