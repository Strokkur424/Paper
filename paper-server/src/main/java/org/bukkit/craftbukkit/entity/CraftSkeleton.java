package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Skeleton;

public class CraftSkeleton extends CraftAbstractSkeleton implements Skeleton {

    public CraftSkeleton(CraftServer server, net.minecraft.world.entity.monster.skeleton.Skeleton entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.monster.skeleton.Skeleton getHandle() {
        return (net.minecraft.world.entity.monster.skeleton.Skeleton) this.entity;
    }

    @Override
    public boolean isConverting() {
        return this.getHandle().isFreezeConverting();
    }

    @Override
    public int getConversionTime() {
        Preconditions.checkState(this.isConverting(), "Entity is not converting");
        return this.getHandle().getConversionTime();
    }

    @Override
    public void setConversionTime(int time) {
        if (time < 0) {
            this.getHandle().setFreezeConverting(false);
        } else {
            this.getHandle().startFreezeConversion(time);
        }
    }

    @Override
    public SkeletonType getSkeletonType() {
       return SkeletonType.NORMAL;
    }

    @Override
    public int inPowderedSnowTime() {
        // Paper - vanilla no longer tracks a powder snow tick counter, only a boolean in/was-in-powder-snow flag
        throw new UnsupportedOperationException("inPowderedSnowTime is no longer tracked by the server");
    }
}
