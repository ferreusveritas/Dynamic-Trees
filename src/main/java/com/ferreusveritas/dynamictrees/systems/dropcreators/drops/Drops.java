package com.ferreusveritas.dynamictrees.systems.dropcreators.drops;

import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Random;

/**
 * @author Harley O'Connor
 */
@FunctionalInterface
public interface Drops {

    List<ItemStack> appendDrops(List<ItemStack> drops, Random random, int fortune);

    default int getChance(int fortune, int baseChance) {
        int chance = baseChance;
        if (fortune > 0) {
            chance -= 10 << fortune;
            if (chance < 40) {
                chance = 40;
            }
        }
        return chance;
    }

}