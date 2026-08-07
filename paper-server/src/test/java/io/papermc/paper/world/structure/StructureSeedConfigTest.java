package io.papermc.paper.world.structure;

import io.papermc.paper.configuration.PaperConfigurations;
import java.io.File;
import java.lang.reflect.Field;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.support.RegistryHelper;
import org.bukkit.support.environment.AllFeatures;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.spigotmc.SpigotConfig;
import org.spigotmc.SpigotWorldConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AllFeatures
public class StructureSeedConfigTest {

    // Paper - the salt field moved from StructurePlacement to AbstractSpreadingStructurePlacement
    private static int salt(final StructurePlacement placement) {
        return ((net.minecraft.world.level.levelgen.structure.placement.AbstractSpreadingStructurePlacement) placement).salt();
    }

    @Test
    public void checkStructureSeedDefaults() throws ReflectiveOperationException {
        SpigotConfig.config = new YamlConfiguration() {
            @Override
            public void save(final @NotNull File file) {
                // no-op
            }
        };
        final SpigotWorldConfig config = PaperConfigurations.SPIGOT_WORLD_DEFAULTS.get();


        final Registry<StructureSet> structureSets = RegistryHelper.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET);
        for (final ResourceKey<StructureSet> setKey : structureSets.registryKeySet()) {
            assertEquals(Identifier.DEFAULT_NAMESPACE, setKey.identifier().getNamespace());
            final StructureSet set = structureSets.getValueOrThrow(setKey);
            if (setKey == BuiltinStructureSets.STRONGHOLDS) { // special case due to seed matching world seed
                assertEquals(0, salt(set.placement()));
                continue;
            }
            int salt = switch (setKey.identifier().getPath()) {
                case "villages" -> config.villageSeed;
                case "desert_pyramids" -> config.desertSeed;
                case "igloos" -> config.iglooSeed;
                case "jungle_temples" -> config.jungleSeed;
                case "swamp_huts" -> config.swampSeed;
                case "pillager_outposts" -> config.outpostSeed;
                case "ocean_monuments" -> config.monumentSeed;
                case "woodland_mansions" -> config.mansionSeed;
                case "buried_treasures" -> config.buriedTreasureSeed;
                case "mineshafts" -> config.mineshaftSeed == null ? 0 : config.mineshaftSeed; // mineshaft seed is set differently
                case "ruined_portals" -> config.portalSeed;
                case "shipwrecks" -> config.shipwreckSeed;
                case "ocean_ruins" -> config.oceanSeed;
                case "nether_complexes" -> config.netherSeed;
                case "nether_fossils" -> config.fossilSeed;
                case "end_cities" -> config.endCitySeed;
                case "ancient_cities" -> config.ancientCitySeed;
                case "trail_ruins" -> config.trailRuinsSeed;
                case "trial_chambers" -> config.trialChambersSeed;
                default -> throw new AssertionError("Missing structure set seed in SpigotWorldConfig for " + setKey);
            };
            if (setKey == BuiltinStructureSets.BURIED_TREASURES) {
                final Field field = net.minecraft.world.level.levelgen.structure.placement.AbstractSpreadingStructurePlacement.class.getDeclaredField("HIGHLY_ARBITRARY_RANDOM_SALT"); // Paper - moved from StructurePlacement
                field.trySetAccessible();
                assertEquals(0, salt(set.placement()));
                assertEquals(field.get(null), salt, "Mismatched default seed for " + setKey + ". Should be " + field.get(null));
                continue;
            }
            assertEquals(salt(set.placement()), salt, "Mismatched default seed for " + setKey + ". Should be " + salt(set.placement()));
        }
    }
}
