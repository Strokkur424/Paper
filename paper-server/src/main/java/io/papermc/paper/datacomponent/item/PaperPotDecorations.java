package io.papermc.paper.datacomponent.item;

import java.util.Optional;
import org.bukkit.craftbukkit.inventory.CraftItemType;
import org.bukkit.craftbukkit.util.Handleable;
import org.bukkit.inventory.ItemType;
import org.jspecify.annotations.Nullable;

public record PaperPotDecorations(
    net.minecraft.world.level.block.entity.PotDecorations impl
) implements PotDecorations, Handleable<net.minecraft.world.level.block.entity.PotDecorations> {

    @Override
    public @Nullable ItemType back() {
        return this.impl.back().map(template -> template.item().value()).map(CraftItemType::minecraftToBukkitNew).orElse(null);
    }

    @Override
    public @Nullable ItemType left() {
        return this.impl.left().map(template -> template.item().value()).map(CraftItemType::minecraftToBukkitNew).orElse(null);
    }

    @Override
    public @Nullable ItemType right() {
        return this.impl.right().map(template -> template.item().value()).map(CraftItemType::minecraftToBukkitNew).orElse(null);
    }

    @Override
    public @Nullable ItemType front() {
        return this.impl.front().map(template -> template.item().value()).map(CraftItemType::minecraftToBukkitNew).orElse(null);
    }

    @Override
    public net.minecraft.world.level.block.entity.PotDecorations getHandle() {
        return this.impl;
    }

    private static net.minecraft.world.item.ItemStackTemplate toTemplate(final net.minecraft.core.Holder<net.minecraft.world.item.Item> item) {
        return new net.minecraft.world.item.ItemStackTemplate(item, 1, net.minecraft.core.component.DataComponentPatch.EMPTY);
    }

    static final class BuilderImpl implements PotDecorations.Builder {

        private @Nullable ItemType back;
        private @Nullable ItemType left;
        private @Nullable ItemType right;
        private @Nullable ItemType front;

        @Override
        public PotDecorations.Builder back(final @Nullable ItemType back) {
            this.back = back;
            return this;
        }

        @Override
        public PotDecorations.Builder left(final @Nullable ItemType left) {
            this.left = left;
            return this;
        }

        @Override
        public PotDecorations.Builder right(final @Nullable ItemType right) {
            this.right = right;
            return this;
        }

        @Override
        public PotDecorations.Builder front(final @Nullable ItemType front) {
            this.front = front;
            return this;
        }

        @Override
        public PotDecorations build() {
            if (this.back == null && this.left == null && this.right == null && this.front == null) {
                return new PaperPotDecorations(net.minecraft.world.level.block.entity.PotDecorations.EMPTY);
            }

            return new PaperPotDecorations(new net.minecraft.world.level.block.entity.PotDecorations(
                Optional.ofNullable(this.back).map(CraftItemType::bukkitToMinecraftHolderNew).map(PaperPotDecorations::toTemplate),
                Optional.ofNullable(this.left).map(CraftItemType::bukkitToMinecraftHolderNew).map(PaperPotDecorations::toTemplate),
                Optional.ofNullable(this.right).map(CraftItemType::bukkitToMinecraftHolderNew).map(PaperPotDecorations::toTemplate),
                Optional.ofNullable(this.front).map(CraftItemType::bukkitToMinecraftHolderNew).map(PaperPotDecorations::toTemplate)
            ));
        }
    }
}
