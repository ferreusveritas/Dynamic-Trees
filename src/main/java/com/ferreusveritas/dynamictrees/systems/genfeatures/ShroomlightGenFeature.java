package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.api.IPostGrowFeature;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.systems.BranchConnectables;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.GenFeatureProperty;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.LinkedList;
import java.util.List;

/**
 * Gen feature for shroomlight but works for any block.
 * Can be fully customized with a custom predicate for natural growth.
 * It is recommended for the generated block to be made connectable using {@link com.ferreusveritas.dynamictrees.systems.BranchConnectables#makeBlockConnectable(Block, BranchConnectables.RadiusForConnectionFunction) makeBlockConnectable}
 *
 * @author Max Hyper
 */
public class ShroomlightGenFeature extends GenFeature implements IPostGenFeature, IPostGrowFeature {

    public static final GenFeatureProperty<Block> SHROOMLIGHT_BLOCK = GenFeatureProperty.createBlockProperty("shroomlight");
    public static final GenFeatureProperty<Float> PLACE_CHANCE = GenFeatureProperty.createFloatProperty("place_chance");

    private static final Direction[] HORIZONTALS = CoordUtils.HORIZONTALS;
    private static final double VANILLA_GROW_CHANCE = .005f;

    public ShroomlightGenFeature (ResourceLocation registryName) {
        super(registryName, SHROOMLIGHT_BLOCK, MAX_HEIGHT, CAN_GROW_PREDICATE, PLACE_CHANCE);
    }

    @Override
    protected ConfiguredGenFeature<GenFeature> createDefaultConfiguration() {
        return super.createDefaultConfiguration().with(SHROOMLIGHT_BLOCK, Blocks.SHROOMLIGHT).with(MAX_HEIGHT, 32)
                .with(CAN_GROW_PREDICATE, (world, blockPos) -> world.getRandom().nextFloat() <= VANILLA_GROW_CHANCE).with(PLACE_CHANCE, .4f);
    }

    @Override
    public boolean postGeneration(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState, Float seasonValue, Float seasonFruitProductionFactor) {
        return placeShroomlightsInValidPlace(configuredGenFeature, world, rootPos, true);
    }

    @Override
    public boolean postGrow(ConfiguredGenFeature<?> configuredGenFeature, World world, BlockPos rootPos, BlockPos treePos, Species species, int soilLife, boolean natural) {
        if (!natural || !configuredGenFeature.get(CAN_GROW_PREDICATE).test(world, rootPos.up())) return false;

        return placeShroomlightsInValidPlace(configuredGenFeature, world, rootPos, false);
    }

    private boolean placeShroomlightsInValidPlace(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, boolean worldGen){
        int treeHeight = getTreeHeight(world, rootPos, configuredGenFeature.get(MAX_HEIGHT));
        Block shroomlightBlock = configuredGenFeature.get(SHROOMLIGHT_BLOCK);

        List<BlockPos> validSpaces = findBranchPits(world, rootPos, treeHeight);
        if (validSpaces.size() > 0){
            if (worldGen){
                for (BlockPos chosenSpace : validSpaces){
                    if (world.getRandom().nextFloat() <= configuredGenFeature.get(PLACE_CHANCE))
                        world.setBlockState(chosenSpace, shroomlightBlock.getDefaultState(), 2);
                }
            } else {
                BlockPos chosenSpace = validSpaces.get(world.getRandom().nextInt(validSpaces.size()));
                world.setBlockState(chosenSpace, shroomlightBlock.getDefaultState(), 2);
            }
            return true;
        }
        return false;
    }

    private int getTreeHeight (IWorld world, BlockPos rootPos, int maxHeight){
        for (int i = 1; i < maxHeight; i++) {
            if (!TreeHelper.isBranch(world.getBlockState(rootPos.up(i)))){
                return i-1;
            }
        }
        return maxHeight;
    }

    //Like the BeeNestGenFeature, the valid places are empty blocks under branches next to the trunk.
    private List<BlockPos> findBranchPits (IWorld world, BlockPos rootPos, int maxHeight){
        List<BlockPos> validSpaces = new LinkedList<>();
        for (int y = 2; y < maxHeight; y++){
            BlockPos trunkPos = rootPos.up(y);
            for (Direction dir : HORIZONTALS){
                BlockPos sidePos = trunkPos.offset(dir);
                if ((world.isAirBlock(sidePos) || world.getBlockState(sidePos).getBlock() instanceof DynamicLeavesBlock) && TreeHelper.isBranch(world.getBlockState(sidePos.up())))
                    validSpaces.add(sidePos);
            }
        }
        return validSpaces;
    }

}