package io.papermc.paper.registry.data;

import io.papermc.paper.registry.PaperRegistryBuilder;
import io.papermc.paper.registry.data.util.Conversions;
import java.util.Collections;
import java.util.Map;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.text.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import static io.papermc.paper.registry.data.util.Checks.asArgument;
import static io.papermc.paper.registry.data.util.Checks.asConfigured;

public class PaperTrimMaterialRegistryEntry implements TrimMaterialRegistryEntry {

    protected final Conversions conversions;
    protected @Nullable Identifier paletteId;
    protected net.minecraft.network.chat.@Nullable Component description;

    public PaperTrimMaterialRegistryEntry(final Conversions conversions, final @Nullable TrimMaterial internal) {
        this.conversions = conversions;
        if (internal == null) {
            return;
        }

        this.paletteId = internal.paletteId();
        this.description = internal.description();
    }

    @Override
    @KeyPattern.Value
    public String baseAssetPath() {
        // Paper - the per-equipment-asset trim texture system was replaced by a single palette id
        @Subst("suffix") final String path = asConfigured(this.paletteId, "baseAssetPath").getPath();
        return path;
    }

    @Override
    public @Unmodifiable Map<Key, String> assetPathOverrides() {
        // Paper - per-equipment-asset trim texture overrides no longer exist in vanilla
        return Collections.emptyMap();
    }

    @Override
    public Component description() {
        return this.conversions.asAdventure(asConfigured(this.description, "description"));
    }

    public static final class PaperBuilder extends PaperTrimMaterialRegistryEntry implements Builder, PaperRegistryBuilder<TrimMaterial, org.bukkit.inventory.meta.trim.TrimMaterial> {

        public PaperBuilder(final Conversions conversions, final @Nullable TrimMaterial internal) {
            super(conversions, internal);
        }

        @Override
        public Builder baseAssetPath(final String baseAssetPath) {
            this.paletteId = Identifier.withDefaultNamespace(asArgument(baseAssetPath, "baseAssetPath"));
            return this;
        }

        @Override
        public Builder assetPathOverrides(final Map<Key, String> assetPathOverrides) {
            // Paper - per-equipment-asset trim texture overrides no longer exist in vanilla; only an empty map is a no-op
            final Map<Key, String> input = asArgument(assetPathOverrides, "assetPathOverrides");
            if (!input.isEmpty()) {
                throw new UnsupportedOperationException("Per-equipment-asset trim texture overrides are no longer supported by the server");
            }
            return this;
        }

        @Override
        public Builder description(final Component description) {
            this.description = this.conversions.asVanilla(asArgument(description, "description"));
            return this;
        }

        @Override
        public TrimMaterial build() {
            return new TrimMaterial(
                asConfigured(this.paletteId, "baseAssetPath"),
                asConfigured(this.description, "description")
            );
        }
    }
}
