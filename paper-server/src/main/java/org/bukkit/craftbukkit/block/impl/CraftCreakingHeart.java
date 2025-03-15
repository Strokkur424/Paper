/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftCreakingHeart extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.CreakingHeart, org.bukkit.block.data.Orientable {

    public CraftCreakingHeart() {
        super();
    }

    public CraftCreakingHeart(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.type.CraftCreakingHeart

    private static final net.minecraft.world.level.block.state.properties.EnumProperty<?> CREAKING_HEART_STATE = getEnum(net.minecraft.world.level.block.CreakingHeartBlock.class, "creaking_heart_state");
    private static final net.minecraft.world.level.block.state.properties.BooleanProperty NATURAL = getBoolean(net.minecraft.world.level.block.CreakingHeartBlock.class, "natural");

    @Override
    public State getCreakingHeartState() {
        return this.get(CraftCreakingHeart.CREAKING_HEART_STATE, org.bukkit.block.data.type.CreakingHeart.State.class);
    }

    @Override
    public void setCreakingHeartState(State state) {
        this.set(CraftCreakingHeart.CREAKING_HEART_STATE, state);
    }

    @Override
    public boolean isNatural() {
        return this.get(CraftCreakingHeart.NATURAL);
    }

    @Override
    public void setNatural(boolean natural) {
        this.set(CraftCreakingHeart.NATURAL, natural);
    }

    // org.bukkit.craftbukkit.block.data.CraftOrientable

    private static final net.minecraft.world.level.block.state.properties.EnumProperty<?> AXIS = getEnum(net.minecraft.world.level.block.CreakingHeartBlock.class, "axis");

    @Override
    public org.bukkit.Axis getAxis() {
        return this.get(CraftCreakingHeart.AXIS, org.bukkit.Axis.class);
    }

    @Override
    public void setAxis(org.bukkit.Axis axis) {
        this.set(CraftCreakingHeart.AXIS, axis);
    }

    @Override
    public java.util.Set<org.bukkit.Axis> getAxes() {
        return this.getValues(CraftCreakingHeart.AXIS, org.bukkit.Axis.class);
    }
}
