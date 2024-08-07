package com.aki.advanced_industry.mods.industry.tileentities.cables.energy;

import cofh.redstoneflux.api.IEnergyProvider;
import cofh.redstoneflux.api.IEnergyReceiver;
import cofh.redstoneflux.api.IEnergyStorage;
import com.aki.advanced_industry.mods.industry.util.CableConnectionMode;
import com.aki.advanced_industry.mods.industry.util.IEnergyCableConnector;
import com.aki.advanced_industry.mods.industry.util.IMachineConfiguration;
import com.aki.advanced_industry.tile.TileEntityBase;
import com.aki.mcutils.APICore.DataManage.DataListManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public abstract class TileEnergyCableBase extends TileEntityBase implements IEnergyCableConnector, IEnergyReceiver, net.minecraftforge.energy.IEnergyStorage, IMachineConfiguration {
    public int MaxSendEnergy;
    public HashMap<EnumFacing, CableConnectionMode> facingMode = new HashMap<>();

    //
    public HashMap<EnumFacing, CableConnectionMode> renderFacingMode = new HashMap<>();

    public int StorageEnergy = 0;
    public int SendEnergyBase = 0;

    public int EnergyReceivers = 0;

    public long Tick = 0;

    public HashSet<BlockPos> CornerLocation = new HashSet<>();
    public EnumFacing[] CableConnectionFacing = new EnumFacing[6];

    //basic 2000
    //advanced 6000
    //extreme 18000
    //ultimate 144000
    public TileEnergyCableBase(int maxSendEnergy) {
        this.MaxSendEnergy = maxSendEnergy;
        for (EnumFacing facing : EnumFacing.VALUES) {
            facingMode.put(facing, CableConnectionMode.NORMAL);
            renderFacingMode.put(facing, CableConnectionMode.CLOSE);
        }
    }

    @Override
    public void update() {
        super.update();
        if (!world.isRemote) {
            Tick++;
            boolean will_update = false;
            CornerLocation.clear();
            if (Tick % 10 == 0) {
                this.EnergyReceivers = 0;
                for (Map.Entry<EnumFacing, CableConnectionMode> entry : facingMode.entrySet()) {
                    CableConnectionFacing[entry.getKey().getIndex()] = null;
                    renderFacingMode.replace(entry.getKey(), entry.getValue());
                    TileEntity tile = world.getTileEntity(this.getPos().add(entry.getKey().getDirectionVec()));
                    EnumFacing facing = entry.getKey();
                    switch (entry.getValue()) {
                        case NORMAL:
                            if (!(tile instanceof TileEnergyCableBase)) {
                                if (tile instanceof IEnergyReceiver) {
                                    if ((((IEnergyReceiver) tile).canConnectEnergy(facing.getOpposite()))) {
                                        this.EnergyReceivers++;
                                    }
                                } else if (tile instanceof IEnergyStorage) {
                                    this.EnergyReceivers++;
                                } else if (tile instanceof net.minecraftforge.energy.IEnergyStorage) {
                                    this.EnergyReceivers++;
                                } else if (tile != null && tile.hasCapability(CapabilityEnergy.ENERGY, facing.getOpposite()) && tile.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite()).canReceive()) {
                                    this.EnergyReceivers++;
                                }
                                will_update = true;
                            }
                            if (tile instanceof TileEnergyCableBase && ((TileEnergyCableBase) tile).facingMode.get(facing.getOpposite()) != CableConnectionMode.CLOSE) {
                                this.CornerLocation.add(this.pos);
                                CableConnectionFacing[entry.getKey().getIndex()] = entry.getKey();
                                ((TileEnergyCableBase) tile).facingMode.replace(facing.getOpposite(), CableConnectionMode.NORMAL);
                                if (this.StorageEnergy > 0)
                                    ((TileEnergyCableBase) tile).EnergyReceiverCountCheck(this.getPos(), this.Tick);
                                will_update = true;
                            }
                            if (!((tile instanceof TileEntityBase) && ((TileEntityBase) tile).canConnectEnergy(facing.getOpposite()) || (tile instanceof IEnergyReceiver && ((IEnergyReceiver) tile).canConnectEnergy(facing.getOpposite())) || tile instanceof IEnergyStorage && !(tile instanceof TileEntityBase) || tile instanceof net.minecraftforge.energy.IEnergyStorage || tile != null && tile.hasCapability(CapabilityEnergy.ENERGY, facing.getOpposite()))) {
                                renderFacingMode.replace(entry.getKey(), CableConnectionMode.CLOSE);
                            }
                            break;
                        case PUSH:
                        case PULL:
                            if (!(tile instanceof TileEnergyCableBase)) {
                                if (tile instanceof IEnergyReceiver) {
                                    if (((IEnergyReceiver) tile).canConnectEnergy(facing.getOpposite())) {
                                        this.EnergyReceivers++;
                                        will_update = true;
                                    }
                                } else if (tile != null && tile.hasCapability(CapabilityEnergy.ENERGY, facing.getOpposite()) && tile.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite()).canReceive()) {
                                    this.EnergyReceivers++;
                                    will_update = true;
                                }
                            }
                            break;
                    }
                }
            }

            CornerLocation.clear();

            if (this.EnergyReceivers > 0) {
                for (Map.Entry<EnumFacing, CableConnectionMode> entry : facingMode.entrySet()) {
                    TileEntity tile = world.getTileEntity(this.getPos().add(entry.getKey().getDirectionVec()));
                    EnumFacing facing = entry.getKey();
                    this.SendEnergyBase = StorageEnergy;
                    switch (entry.getValue()) {
                        case NORMAL:
                            if (this.StorageEnergy <= this.MaxSendEnergy) {
                                if (tile instanceof IEnergyProvider) {
                                    if (((IEnergyProvider) tile).canConnectEnergy(facing.getOpposite())) {
                                        StorageEnergy += ((IEnergyProvider) tile).extractEnergy(facing.getOpposite(), MaxSendEnergy - StorageEnergy, false);
                                    }
                                } else if (tile instanceof IEnergyStorage) {
                                    StorageEnergy += ((IEnergyStorage) tile).extractEnergy(MaxSendEnergy - StorageEnergy, false);
                                } else if (tile instanceof net.minecraftforge.energy.IEnergyStorage) {
                                    StorageEnergy += ((net.minecraftforge.energy.IEnergyStorage) tile).extractEnergy(MaxSendEnergy - StorageEnergy, false);
                                } else if (tile != null && tile.hasCapability(CapabilityEnergy.ENERGY, facing.getOpposite())) {
                                    StorageEnergy += tile.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite()).extractEnergy(MaxSendEnergy - StorageEnergy, false);
                                }
                                will_update = true;
                            }
                            if (!(tile instanceof TileEnergyCableBase)) {
                                if (tile instanceof IEnergyReceiver) {
                                    if (((IEnergyReceiver) tile).canConnectEnergy(facing.getOpposite())) {
                                        StorageEnergy -= ((IEnergyReceiver) tile).receiveEnergy(facing.getOpposite(), this.DividedNotRemainder(Math.min(MaxSendEnergy, SendEnergyBase), this.EnergyReceivers), false);
                                    }
                                } else if (tile instanceof IEnergyStorage) {
                                    StorageEnergy -= ((IEnergyStorage) tile).receiveEnergy(this.DividedNotRemainder(Math.min(MaxSendEnergy, SendEnergyBase), this.EnergyReceivers), false);
                                } else if (tile instanceof net.minecraftforge.energy.IEnergyStorage) {
                                    StorageEnergy -= ((net.minecraftforge.energy.IEnergyStorage) tile).receiveEnergy(this.DividedNotRemainder(Math.min(MaxSendEnergy, SendEnergyBase), this.EnergyReceivers), false);
                                } else if (tile != null && tile.hasCapability(CapabilityEnergy.ENERGY, facing.getOpposite()) && tile.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite()).canReceive()) {
                                    StorageEnergy -= tile.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite()).receiveEnergy(this.DividedNotRemainder(Math.min(MaxSendEnergy, SendEnergyBase), this.EnergyReceivers), false);
                                }
                                will_update = true;
                            }
                            if (tile instanceof TileEnergyCableBase && ((TileEnergyCableBase) tile).facingMode.get(facing.getOpposite()) != CableConnectionMode.CLOSE && this.StorageEnergy > 0) {
                                this.CornerLocation.add(this.pos);
                                ((TileEnergyCableBase) tile).SendEnergy(this.getPos(), this.Tick, Math.min(((TileEnergyCableBase) tile).MaxSendEnergy, this.MaxSendEnergy));
                                will_update = true;
                            }
                            break;
                        case PUSH:
                            if (!(tile instanceof TileEnergyCableBase)) {
                                if (tile instanceof IEnergyReceiver) {
                                    if (((IEnergyReceiver) tile).canConnectEnergy(facing.getOpposite())) {
                                        StorageEnergy -= ((IEnergyReceiver) tile).receiveEnergy(facing.getOpposite(), this.DividedNotRemainder(Math.min(MaxSendEnergy, SendEnergyBase), this.EnergyReceivers), false);
                                    }
                                } else if (tile instanceof IEnergyStorage) {
                                    StorageEnergy -= ((IEnergyStorage) tile).receiveEnergy(this.DividedNotRemainder(Math.min(MaxSendEnergy, SendEnergyBase), this.EnergyReceivers), false);
                                } else if (tile instanceof net.minecraftforge.energy.IEnergyStorage) {
                                    StorageEnergy -= ((net.minecraftforge.energy.IEnergyStorage) tile).receiveEnergy(this.DividedNotRemainder(Math.min(MaxSendEnergy, SendEnergyBase), this.EnergyReceivers), false);
                                } else if (tile != null && tile.hasCapability(CapabilityEnergy.ENERGY, facing.getOpposite()) && tile.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite()).canReceive()) {
                                    StorageEnergy -= tile.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite()).receiveEnergy(this.DividedNotRemainder(Math.min(MaxSendEnergy, SendEnergyBase), this.EnergyReceivers), false);
                                }
                                will_update = true;
                            }
                            /*if (!(tile instanceof TileEnergyCableBase)) {
                                if (tile instanceof IEnergyReceiver) {
                                    if (((IEnergyReceiver) tile).canConnectEnergy(facing.getOpposite())) {
                                        StorageEnergy -= ((IEnergyReceiver) tile).receiveEnergy(facing.getOpposite(), this.DividedNotRemainder(Math.min(MaxSendEnergy, SendEnergyBase), this.EnergyReceivers), false);
                                    }
                                } else if (tile != null && tile.hasCapability(CapabilityEnergy.ENERGY, facing.getOpposite())) {
                                    StorageEnergy -= tile.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite()).receiveEnergy(this.DividedNotRemainder(Math.min(MaxSendEnergy, SendEnergyBase), this.EnergyReceivers), false);
                                }
                            }*/
                            break;
                        case PULL:
                            if (tile instanceof IEnergyProvider) {
                                if (((IEnergyProvider) tile).canConnectEnergy(facing.getOpposite())) {
                                    StorageEnergy += ((IEnergyProvider) tile).extractEnergy(facing.getOpposite(), MaxSendEnergy - StorageEnergy, false);
                                    will_update = true;
                                }
                            } else if (tile instanceof IEnergyStorage) {
                                StorageEnergy += ((IEnergyStorage) tile).extractEnergy(MaxSendEnergy - StorageEnergy, false);
                                will_update = true;
                            } else if (tile instanceof net.minecraftforge.energy.IEnergyStorage) {
                                StorageEnergy += ((net.minecraftforge.energy.IEnergyStorage) tile).extractEnergy(MaxSendEnergy - StorageEnergy, false);
                                will_update = true;
                            } else if (tile != null && tile.hasCapability(CapabilityEnergy.ENERGY, facing.getOpposite())) {
                                StorageEnergy += tile.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite()).extractEnergy(MaxSendEnergy - StorageEnergy, false);
                                will_update = true;
                            }
                            break;
                    }
                }
            }
            if(will_update)
                this.sendUpdates();
        }
    }

    @Override
    public void validate() {
        super.validate();
    }

    @Override
    public boolean canConnectEnergy(EnumFacing enumFacing) {
        return this.facingMode.get(enumFacing) != CableConnectionMode.CLOSE;
    }

    /**
     * 送っている最中、電気を使った場合、供給元の電気を減らす?
     */
    @Override//電力を送っている途中
    public void SendEnergy(BlockPos StartPos, long tick, int maxSendEnergy) {
        TileEntity StartTile = world.getTileEntity(StartPos);
        if (StartTile instanceof TileEnergyCableBase) {
            if (!((TileEnergyCableBase) StartTile).CornerLocation.contains(this.pos)) {
                ((TileEnergyCableBase) StartTile).CornerLocation.add(this.pos);
                for (Map.Entry<EnumFacing, CableConnectionMode> entry : this.facingMode.entrySet()) {
                    EnumFacing facing = entry.getKey();
                    TileEntity tile = world.getTileEntity(this.getPos().add(facing.getDirectionVec()));

                    switch (entry.getValue()) {
                        case NORMAL:
                        case PUSH:
                            if (!(tile instanceof TileEnergyCableBase)) {
                                if (tile instanceof IEnergyReceiver) {
                                    if (((IEnergyReceiver) tile).canConnectEnergy(facing.getOpposite())) {
                                        ((TileEnergyCableBase) StartTile).StorageEnergy -= ((IEnergyReceiver) tile).receiveEnergy(facing.getOpposite(), this.DividedNotRemainder(Math.min(maxSendEnergy, ((TileEnergyCableBase) StartTile).SendEnergyBase), ((TileEnergyCableBase) StartTile).EnergyReceivers), false);
                                    }
                                } else if (tile instanceof IEnergyStorage) {
                                    ((TileEnergyCableBase) StartTile).StorageEnergy -= ((IEnergyStorage) tile).receiveEnergy(this.DividedNotRemainder(Math.min(maxSendEnergy, ((TileEnergyCableBase) StartTile).SendEnergyBase), ((TileEnergyCableBase) StartTile).EnergyReceivers), false);
                                } else if (tile instanceof net.minecraftforge.energy.IEnergyStorage) {
                                    ((TileEnergyCableBase) StartTile).StorageEnergy -= ((net.minecraftforge.energy.IEnergyStorage) tile).receiveEnergy(this.DividedNotRemainder(Math.min(maxSendEnergy, ((TileEnergyCableBase) StartTile).SendEnergyBase), ((TileEnergyCableBase) StartTile).EnergyReceivers), false);
                                } else if (tile != null && tile.hasCapability(CapabilityEnergy.ENERGY, facing.getOpposite())) {
                                    ((TileEnergyCableBase) StartTile).StorageEnergy -= tile.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite()).receiveEnergy(this.DividedNotRemainder(Math.min(maxSendEnergy, ((TileEnergyCableBase) StartTile).SendEnergyBase), ((TileEnergyCableBase) StartTile).EnergyReceivers), false);
                                }
                            }

                            if (tile instanceof TileEnergyCableBase && ((TileEnergyCableBase) tile).facingMode.get(facing.getOpposite()) != CableConnectionMode.CLOSE) {
                                long nextTick = ((TileEnergyCableBase) tile).Tick;
                                if (nextTick != tick && nextTick != this.Tick) {//前のTickと今のTickとは次が違う場合
                                    if (((TileEnergyCableBase) StartTile).StorageEnergy > 0) {
                                        ((TileEnergyCableBase) tile).SendEnergy(StartPos, this.Tick, Math.min(maxSendEnergy, ((TileEnergyCableBase) tile).MaxSendEnergy));
                                    }
                                }
                            }
                            break;
                    }
                }
            }
        }
    }

    @Override
    public void EnergyReceiverCountCheck(BlockPos StartPos, long tick) {
        TileEntity StartTile = world.getTileEntity(StartPos);
        if (StartTile instanceof TileEnergyCableBase) {
            if (!((TileEnergyCableBase) StartTile).CornerLocation.contains(this.pos)) {
                ((TileEnergyCableBase) StartTile).CornerLocation.add(this.pos);
                for (Map.Entry<EnumFacing, CableConnectionMode> entry : this.facingMode.entrySet()) {
                    EnumFacing facing = entry.getKey();
                    TileEntity tile = world.getTileEntity(this.getPos().add(facing.getDirectionVec()));

                    switch (entry.getValue()) {
                        case NORMAL:
                        case PUSH:
                            if (!(tile instanceof TileEnergyCableBase)) {
                                if (tile instanceof IEnergyReceiver) {
                                    if (((IEnergyReceiver) tile).canConnectEnergy(facing.getOpposite())) {
                                        ((TileEnergyCableBase) StartTile).EnergyReceivers++;
                                    }
                                } else if (tile instanceof IEnergyStorage) {
                                    ((TileEnergyCableBase) StartTile).EnergyReceivers++;
                                } else if (tile instanceof net.minecraftforge.energy.IEnergyStorage) {
                                    ((TileEnergyCableBase) StartTile).EnergyReceivers++;
                                } else if (tile != null && tile.hasCapability(CapabilityEnergy.ENERGY, facing.getOpposite())) {
                                    ((TileEnergyCableBase) StartTile).EnergyReceivers++;
                                }
                            }

                            if (tile instanceof TileEnergyCableBase) {
                                long nextTick = ((TileEnergyCableBase) tile).Tick;
                                if (nextTick != tick && nextTick != this.Tick) {//前のTickと今のTickとは次が違う場合
                                    if (((TileEnergyCableBase) StartTile).StorageEnergy > 0) {
                                        ((TileEnergyCableBase) tile).EnergyReceiverCountCheck(StartPos, this.Tick);
                                    }
                                }
                            }
                            break;
                    }
                }
            }
        }
    }

    private int DividedNotRemainder(int Input, int by) {
        int Remainder = Input % by;
        return (Input - Remainder) / by;
    }

    @Override
    public int getEnergyStored(EnumFacing enumFacing) {
        return this.StorageEnergy;
    }

    @Override
    public int getMaxEnergyStored(EnumFacing enumFacing) {
        return MaxSendEnergy;
    }

    @Override
    public int receiveEnergy(int i, boolean b) {
        int ReceiveEnergy = Math.min(i, this.MaxSendEnergy - this.StorageEnergy);
        if (!b)
            this.StorageEnergy += ReceiveEnergy;
        return ReceiveEnergy;
    }

    @Override
    public int extractEnergy(int i, boolean b) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return this.StorageEnergy;
    }

    @Override
    public int getMaxEnergyStored() {
        return this.MaxSendEnergy;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    @Override
    public int receiveEnergy(EnumFacing enumFacing, int i, boolean b) {
        /*if (this.facingMode.get(enumFacing) != CableConnectionMode)
            return 0;*/
        int ReceiveEnergy = Math.min(i, this.MaxSendEnergy - this.StorageEnergy);
        if (!b)
            this.StorageEnergy += ReceiveEnergy;
        return ReceiveEnergy;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.StorageEnergy = compound.getInteger("Energy");
        this.EnergyReceivers = compound.getInteger("EnergyReceivers");
        this.Tick = compound.getLong("Tick");
        for (EnumFacing facing : EnumFacing.VALUES) {
            this.facingMode.replace(facing, CableConnectionMode.getCableMode(compound.getInteger("CableModeIndex_" + facing.getName())));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("Energy", this.StorageEnergy);
        compound.setInteger("EnergyReceivers", this.EnergyReceivers);
        compound.setLong("Tick", this.Tick);
        for (EnumFacing facing : EnumFacing.VALUES) {
            compound.setInteger("CableModeIndex_" + facing.getName(), this.facingMode.get(facing).getIndex());
        }
        return compound;
    }

    @Override
    public DataListManager getNetWorkData() {
        DataListManager data = super.getNetWorkData();
        for (EnumFacing facing : this.facingMode.keySet()) {
            data.addData(this.renderFacingMode.get(facing).getIndex());
        }
        return data;
    }

    @Override
    public void ReceivePacketData(DataListManager dataListManager) {
        super.ReceivePacketData(dataListManager);
        for (EnumFacing facing : this.facingMode.keySet()) {
            this.renderFacingMode.replace(facing, CableConnectionMode.getCableMode(dataListManager.getDataInt()));
        }
    }

    public void ChangeFacingState(EnumFacing facing) {
        if (this.CableConnectionFacing[facing.getIndex()] == null) {
            CableConnectionMode mode = this.facingMode.get(facing);
            CableConnectionMode next = CableConnectionMode.values()[(mode.getIndex() + 1) % CableConnectionMode.values().length];
            this.facingMode.replace(facing, next);
        } else {
            if (this.facingMode.get(facing) == CableConnectionMode.NORMAL) {
                this.facingMode.replace(facing, CableConnectionMode.CLOSE);
                TileEntity tile = world.getTileEntity(this.getPos().offset(facing));
                if (tile instanceof TileEnergyCableBase) {
                    ((TileEnergyCableBase) tile).facingMode.replace(facing.getOpposite(), CableConnectionMode.CLOSE);
                }
            } else {
                this.facingMode.replace(facing, CableConnectionMode.NORMAL);
            }
        }

        this.sendUpdates();
    }

    @Override
    public EnumActionResult onSneakRightClick(EntityPlayer player, EnumFacing side, RayTraceResult rayTraceResult) {
        if (!world.isRemote) {
            this.invalidate();
            world.destroyBlock(this.getPos(), true);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public EnumActionResult onRightClick(EntityPlayer player, EnumFacing side, RayTraceResult rayTraceResult) {
        this.ChangeFacingState(side);
        return EnumActionResult.SUCCESS;
    }

    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        return (capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing)) && this.facingMode.get(facing) != CableConnectionMode.CLOSE;
    }

    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        return capability == CapabilityEnergy.ENERGY && this.facingMode.get(facing) != CableConnectionMode.CLOSE ? (T) this : super.getCapability(capability, facing);
        //return super.getCapability(capability, facing);
    }
}
