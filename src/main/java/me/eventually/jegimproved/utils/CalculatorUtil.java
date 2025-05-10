package me.eventually.jegimproved.utils;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.eventually.jegimproved.exceptions.CalculatorOverflow;
import net.guizhanss.guizhanlib.minecraft.helper.inventory.ItemStackHelper;
import org.bukkit.inventory.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for Slimefun Material calculation.
 * Code logic mostly from
 * <a href="https://github.com/Seggan/SFCalc">SFCalc</a>
 * different from the original SFCalc, this class uses BigInteger instead of long to avoid overflow
 */
@ParametersAreNonnullByDefault
public class CalculatorUtil {
    private static final List<String> blackListedIds = List.of(
            "UU_MATTER",
            "SILICON",
            "FALLEN_METEOR",
            "RUBBER",
            "VOID_BIT",
            "CARBON",
            "MOMOTECH_DIAMOND",
            "MOMOTECH_IRON",
            "MOMOTECH_GOLD",
            "MOMOTECH_LAPIS",
            "MOMOTECH_REDSTONE",
            "MOMOTECH_QUARTZ",
            "MOMOTECH_EMERALD",
            "MOMOTECH_COAL",
            "MOMOTECH_DIAMOND1",
            "MOMOTECH_IRON1",
            "MOMOTECH_GOLD1",
            "MOMOTECH_LAPIS1",
            "MOMOTECH_REDSTONE1",
            "MOMOTECH_QUARTZ1",
            "MOMOTECH_EMERALD1",
            "MOMOTECH_COAL1",
            "MOMOTECH_CU",
            "MOMOTECH_AU",
            "MOMOTECH_AL",
            "MOMOTECH_PB",
            "MOMOTECH_PB",
            "MOMOTECH_ZN",
            "MOMOTECH_MG",
            "MOMOTECH_AG",
            "MOMOTECH_CU1",
            "MOMOTECH_AU1",
            "MOMOTECH_AL1",
            "MOMOTECH_PB1",
            "MOMOTECH_SN1",
            "MOMOTECH_ZN1",
            "MOMOTECH_MG1",
            "MOMOTECH_AG1"
    );
    private static final List<RecipeType> blacklistedRecipeTypes = List.of(
            RecipeType.ORE_WASHER,
            RecipeType.GEO_MINER,
            RecipeType.GOLD_PAN,
            RecipeType.MOB_DROP,
            RecipeType.BARTER_DROP,
            RecipeType.ORE_CRUSHER,
            RecipeType.NULL
    );
    public static final BigInteger MAX_AMOUNT= BigInteger.valueOf(10).pow(99);
    private static final ThreadLocal<SlimefunItem> inCalculating = new ThreadLocal<>();
    private CalculatorUtil() {
    }

    public static Map<ItemStack, BigInteger> calculateRecipe(SlimefunItem sfItem, BigInteger amount) throws CalculatorOverflow {
        inCalculating.set(sfItem);
        Map<ItemStack, BigInteger> result = new HashMap<>();

        // Calculate recipe

        BigInteger sfItemRecipeOutput = BigInteger.valueOf(sfItem.getRecipeOutput().getAmount());
        BigInteger operations = amount.add(sfItemRecipeOutput.add(BigInteger.ONE.negate())).divide(sfItemRecipeOutput);
        for (ItemStack itemStack : sfItem.getRecipe()) {
            if (itemStack == null) continue;
            addItem(result, itemStack, operations.multiply(BigInteger.valueOf(itemStack.getAmount())));
        }
        SlimefunItem nextInRecipes = getNextInRecipes(result);
        while (nextInRecipes != null) {
            sfItemRecipeOutput = BigInteger.valueOf(nextInRecipes.getRecipeOutput().getAmount());
            operations = result.get(nextInRecipes.getItem()).add(sfItemRecipeOutput.add(BigInteger.ONE.negate())).divide(sfItemRecipeOutput);
            addItem(result, nextInRecipes.getRecipeOutput(), sfItemRecipeOutput.multiply(operations).negate());
            for (ItemStack itemStack : nextInRecipes.getRecipe()) {
                if (itemStack == null) continue;
                try{
                    addItem(result, itemStack, operations.multiply(BigInteger.valueOf(itemStack.getAmount())));
                } catch (CalculatorOverflow e){
                    throw new CalculatorOverflow(ItemStackHelper.getDisplayName(itemStack));
                }

            }
            nextInRecipes = getNextInRecipes(result);
        }
        inCalculating.remove();
        return result;
    }

    private static void addItem(Map<ItemStack, BigInteger> map, ItemStack itemStack, BigInteger amount) {
        ItemStack clone = itemStack.clone();
        clone.setAmount(1);
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new CalculatorOverflow("Overflow");
        }
        if (map.containsKey(clone)) {
            map.put(clone, map.get(clone).add(amount));
        } else {
            map.put(clone, amount);
        }
    }
    private static SlimefunItem getNextInRecipes(Map<ItemStack, BigInteger> map) {
        for (Map.Entry<ItemStack, BigInteger> entry : map.entrySet()) {
            SlimefunItem sfItem = SlimefunItem.getByItem(entry.getKey());
            if (sfItem != null
                    && !blackListedIds.contains(sfItem.getId())
                    && !blacklistedRecipeTypes.contains(sfItem.getRecipeType())
                    && !sfItem.isDisabled()
                    && !sfItem.isHidden()
                    && !sfItem.equals(inCalculating.get())
            ) {
                if (entry.getValue().compareTo(BigInteger.ZERO) > 0) {
                    return sfItem;
                }
            }
        }
        return null;
    }
}
