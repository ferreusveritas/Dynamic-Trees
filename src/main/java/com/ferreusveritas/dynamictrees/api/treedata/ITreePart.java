package com.ferreusveritas.dynamictrees.api.treedata;

import com.ferreusveritas.dynamictrees.api.network.GrowSignal;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public interface ITreePart {

	/**
	* The level of hydration that this block provides to neighboring structures
	*
	* @param blockAccess Readonly access to blocks
	* @param x X Position
	* @param y Y Position
	* @param z Z Position
	* @param dir The direction of the request(opposite the direction of the requester)
	* @param leavesTree The tree data of the leaves the request came from
	* @return Hydration level for block
	*/
	int getHydrationLevel(IBlockAccess blockAccess, BlockPos pos, EnumFacing dir, DynamicTree leavesTree);

	/**
	* The signal that is passed from the root of the tree to the tip of a branch to create growth.
	* 
	* @param world The current world
	* @param x X Position
	* @param y Y Position
	* @param z Z Position
	* @param signal Signal structure that keeps track of the growth path
	* @return Signal parameter for chaining
	*/
	GrowSignal growSignal(World world, BlockPos pos, GrowSignal signal);

	/**
	* The probability that the branch logic will follow into this block as part of it's path.
	* 
	* @param blockAccess Readonly access to blocks
	* @param x X Position
	* @param y Y Position
	* @param z Z Position
	* @param from The branch making the request
	* @return Probability weight used to determine if the growth path will take this block as a path next. 
	*/
	int probabilityForBlock(IBlockAccess blockAccess, BlockPos pos, BlockBranch from);

	/**
	* The radius of the part that a neighbor is expected to connect with 
	* @param world The current world
	* @param x X Position
	* @param y Y Position
	* @param z Z Position
	* @param branch The branch making the request
	* @param fromRadius The radius of the branch requesting connection data
	* @return Radius of the connection point to this block from the branch
	*/
	int getRadiusForConnection(IBlockAccess world, BlockPos pos, BlockBranch from, int fromRadius);

	/**
	* Used to get the radius of branches.. all other treeparts will/should return 0
	* @param blockAccess Readonly access to blocks
	* @param x X Position
	* @param y Y Position
	* @param z Z Position
	* @return Radius of the treepart(branch)
	*/
	int getRadius(IBlockAccess blockAccess, BlockPos pos);

	/**
	* Configurable general purpose branch network scanner to gather data and/or perform operations
	* 
	* @param world The current world
	* @param x X Position
	* @param y Y Position
	* @param z Z Position
	* @param fromDir The direction that should not be analyzed.  Pass ForgeDirection.UNKNOWN to analyse in all directions
	* @param signal The Mapping Signal object to gather data and/or perform operations
	* @return
	*/
	MapSignal analyse(World world, BlockPos pos, EnumFacing fromDir, MapSignal signal);

	/**
	* Get the appropriate dynamic tree this block is used to build.
	*  
	* @param blockAccess Readonly access to blocks
	* @param x X Position
	* @param y Y Position
	* @param z Z Position
	* @return DynamicTree
	*/
	DynamicTree getTree(IBlockAccess blockAccess, BlockPos pos);

	/**
	* A branch requires 2 or more adjacent supporting neighbors at least one of which must be another branch
	* Valid supports are other branches(always), leaves(for twigs), and rooty dirt(under special circumstances)
	* return value is nybbled neighbor values. High Nybble(0xF0) is count of branches only, Low Nybble(0x0F) is any valid reinforcing treepart(including branches)
	* 
	* @param blockAccess Readonly access to blocks
	* @param branch The branch making the request
	* @param x X Position
	* @param y Y Position
	* @param z Z Position
	* @param dir The direction of the request(opposite the direction of the requester)
	* @param radius The radius of the branch requesting support
	* @return Neighbor values in Nybble pair ( (#branches & 0xF0) | (#treeparts & 0x0F) )
	*/
	int branchSupport(IBlockAccess blockAccess, BlockBranch branch, BlockPos pos, EnumFacing dir, int radius);

	/**
	* Apply an item to the treepart(e.g. bonemeal). Developer is responsible for decrementing itemStack after applying.
	* 
	* @param world The current world
	* @param x X Position
	* @param y Y Position
	* @param z Z Position
	* @param player The player applying the substance
	* @param itemStack The itemstack to be used.
	* @return true if item was used, false otherwise
	*/
	public boolean applyItemSubstance(World world, BlockPos pos, EntityPlayer player, EnumHand hand, ItemStack itemStack);

	/**
	* The single root node of a tree.
	* 
	* @return true if treepart is root node. false otherwise.
	*/
	boolean isRootNode();

}