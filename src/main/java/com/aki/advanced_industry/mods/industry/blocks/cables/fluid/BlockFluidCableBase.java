package com.aki.advanced_industry.mods.industry.blocks.cables.fluid;

import com.aki.advanced_industry.ModMaterials;
import com.aki.advanced_industry.block.BlockBase;
import com.aki.advanced_industry.mods.industry.tileentities.cables.fluid.TileFluidCableBase;
import com.aki.advanced_industry.mods.industry.util.CableConnectionMode;
import com.aki.advanced_industry.mods.industry.util.IBlockFacingBound;
import com.google.common.collect.Lists;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class BlockFluidCableBase extends BlockBase implements IBlockFacingBound {
    public AxisAlignedBB boundBox = FULL_BLOCK_AABB;
    public AxisAlignedBB baseBox = new AxisAlignedBB(0.3125F, 0.3125F, 0.3125F, 0.6875F, 0.6875F, 0.6875F);
    public BlockFluidCableBase(Material m) {
        super(m);
        this.blockHardness = 3.0F;
        this.blockResistance = 5.0F;
        this.blockSoundType = SoundType.STONE;
        this.setCreativeTab(ModMaterials.tabs);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return true;
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
        /*AxisAlignedBB baseBox = new AxisAlignedBB(0.3125F, 0.3125F, 0.3125F, 0.6875F, 0.6875F, 0.6875F)
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof TileFluidCableBase) {
            for (Map.Entry<EnumFacing, CableConnectionMode> entry : ((TileFluidCableBase) tile).renderFacingMode.entrySet()) {
                switch (entry.getKey()) {
                    case UP:
                        if(entry.getValue() != CableConnectionMode.CLOSE)
                            baseBox = new AxisAlignedBB(baseBox.minX, baseBox.minY, baseBox.minZ, baseBox.maxX, baseBox.maxY + 0.3125, baseBox.maxZ);
                        break;
                    case DOWN:
                        if(entry.getValue() != CableConnectionMode.CLOSE)
                            baseBox = new AxisAlignedBB(baseBox.minX, 0, baseBox.minZ, baseBox.maxX, baseBox.maxY, baseBox.maxZ);
                        break;
                    case NORTH:
                        if(entry.getValue() != CableConnectionMode.CLOSE)
                            baseBox = new AxisAlignedBB(baseBox.minX, baseBox.minY, 0, baseBox.maxX, baseBox.maxY, baseBox.maxZ);
                        break;
                    case EAST:
                        if(entry.getValue() != CableConnectionMode.CLOSE)
                            baseBox = new AxisAlignedBB(baseBox.minX, baseBox.minY, baseBox.minZ, baseBox.maxX + 0.3125, baseBox.maxY, baseBox.maxZ);
                        break;
                    case WEST:
                        if(entry.getValue() != CableConnectionMode.CLOSE)
                            baseBox = new AxisAlignedBB(0, baseBox.minY, baseBox.minZ, baseBox.maxX, baseBox.maxY + 0.3125, baseBox.maxZ);
                        break;
                    case SOUTH:
                        if(entry.getValue() != CableConnectionMode.CLOSE)
                            baseBox = new AxisAlignedBB(baseBox.minX, baseBox.minY, baseBox.minZ, baseBox.maxX, baseBox.maxY, baseBox.maxZ + 0.3125);
                        break;
                }
            }
        }
        return baseBox;*/
        return boundBox.offset(pos);
    }

    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        addCollisionBoxToList(pos, entityBox, collidingBoxes, baseBox);
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof TileFluidCableBase) {
            for (Map.Entry<EnumFacing, CableConnectionMode> entry : ((TileFluidCableBase) tile).renderFacingMode.entrySet()) {
                if(entry.getValue() != CableConnectionMode.CLOSE)
                    addCollisionBoxToList(pos, entityBox, collidingBoxes, this.getFacingBoundingBox(worldIn, pos).get(entry.getKey()));
                /*switch (entry.getKey()) {
                    case UP:
                        if(entry.getValue() != CableConnectionMode.CLOSE)
                            addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.3125F, 0.3125F, 0.3125F, 0.6875F, 1.0F, 0.6875F));
                        break;
                    case DOWN:
                        if(entry.getValue() != CableConnectionMode.CLOSE)
                            addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.3125F, 0.0F, 0.3125F, 0.6875F, 0.3125F, 0.6875F));
                        break;
                    case NORTH:
                        if(entry.getValue() != CableConnectionMode.CLOSE)
                            addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.3125F, 0.3125F, 0.0F, 0.6875F, 0.6875F, 0.375));
                        break;
                    case EAST:
                        if(entry.getValue() != CableConnectionMode.CLOSE)
                            addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.6875F, 0.3125F, 0.3125F, 1.0F, 0.6875F, 0.6875F));
                        break;
                    case WEST:
                        if(entry.getValue() != CableConnectionMode.CLOSE)
                            addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.0F, 0.3125F, 0.3125F, 0.3125F, 0.6875F, 0.6875F));
                        break;
                    case SOUTH:
                        if(entry.getValue() != CableConnectionMode.CLOSE)
                            addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.3125F, 0.3125F, 0.6875F, 0.6875F, 0.6875F, 1.0F));
                        break;
                }*/
            }
        }
    }

    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
        List<AxisAlignedBB> aabbList = new ArrayList<>(Lists.newArrayList(baseBox));
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof TileFluidCableBase) {
            for (Map.Entry<EnumFacing, CableConnectionMode> entry : ((TileFluidCableBase) tile).renderFacingMode.entrySet()) {
                if(entry.getValue() != CableConnectionMode.CLOSE)
                    aabbList.add(getFacingBoundingBox(worldIn, pos).get(entry.getKey()));
            }
        }

        List<RayTraceResult> rayTraceResults = new ArrayList<>();
        for(AxisAlignedBB alignedBB : aabbList) {
            rayTraceResults.add(this.rayTrace(pos, start, end, alignedBB));
        }

        RayTraceResult longRay = null;
        double dist = 0;
        for (int i = 0; i < rayTraceResults.size(); i++ ) {
            if (rayTraceResults.get(i) != null) {
                if (rayTraceResults.get(i).hitVec.squareDistanceTo(end) > dist) {
                    longRay = rayTraceResults.get(i);
                    dist = rayTraceResults.get(i).hitVec.squareDistanceTo(end);
                    this.boundBox = aabbList.get(i);
                }
            }
        }


        return longRay;
    }

    @Override
    public LinkedHashMap<EnumFacing, AxisAlignedBB> getFacingBoundingBox(World world, BlockPos pos) {
        LinkedHashMap<EnumFacing, AxisAlignedBB> facingMap = new LinkedHashMap<>();
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof TileFluidCableBase) {
            if (((TileFluidCableBase) tile).renderFacingMode.get(EnumFacing.DOWN) != CableConnectionMode.CLOSE)
                facingMap.put(EnumFacing.DOWN, new AxisAlignedBB(0.3125F, 0.0F, 0.3125F, 0.6875F, 0.3125F, 0.6875F));
            if (((TileFluidCableBase) tile).renderFacingMode.get(EnumFacing.UP) != CableConnectionMode.CLOSE)
                facingMap.put(EnumFacing.UP, new AxisAlignedBB(0.3125F, 0.3125F, 0.3125F, 0.6875F, 1.0F, 0.6875F));
            if (((TileFluidCableBase) tile).renderFacingMode.get(EnumFacing.NORTH) != CableConnectionMode.CLOSE)
                facingMap.put(EnumFacing.NORTH, new AxisAlignedBB(0.3125F, 0.3125F, 0.0F, 0.6875F, 0.6875F, 0.375));
            if (((TileFluidCableBase) tile).renderFacingMode.get(EnumFacing.SOUTH) != CableConnectionMode.CLOSE)
                facingMap.put(EnumFacing.SOUTH, new AxisAlignedBB(0.3125F, 0.3125F, 0.6875F, 0.6875F, 0.6875F, 1.0F));
            if (((TileFluidCableBase) tile).renderFacingMode.get(EnumFacing.WEST) != CableConnectionMode.CLOSE)
                facingMap.put(EnumFacing.WEST, new AxisAlignedBB(0.0F, 0.3125F, 0.3125F, 0.3125F, 0.6875F, 0.6875F));
            if (((TileFluidCableBase) tile).renderFacingMode.get(EnumFacing.EAST) != CableConnectionMode.CLOSE)
                facingMap.put(EnumFacing.EAST, new AxisAlignedBB(0.6875F, 0.3125F, 0.3125F, 1.0F, 0.6875F, 0.6875F));
        }
        return facingMap;
    }
}