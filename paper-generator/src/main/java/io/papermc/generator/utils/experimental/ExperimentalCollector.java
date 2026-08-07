package io.papermc.generator.utils.experimental;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import io.papermc.generator.Main;
import io.papermc.generator.utils.Formatting;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.TradeRebalanceRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@NullMarked
public final class ExperimentalCollector {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Paper - the vanilla RegistrySetBuilder no longer exposes the raw bootstrap function of a
    // RegistryStub (it only exposes RegistryStub#apply(BuildState), which is opaque), so instead of
    // re-running the bootstrap against a fake recording BootstrapContext, each stub is built in
    // isolation via the real RegistrySetBuilder#build and the resulting registry's key set is read back.
    private static final Map<ResourceKey<? extends Registry<?>>, RegistrySetBuilder.RegistryStub> VANILLA_REGISTRY_ENTRIES = Stream.concat(
            VanillaRegistries.WORLD_BUILDER.entries.stream(), VanillaRegistries.RELOADABLE_BUILDER.entries.stream()
        )
        .collect(Collectors.toMap(stub -> stub.requiredRegistries().findFirst().orElseThrow(), stub -> stub));

    private static final Map<RegistrySetBuilder, SingleFlagHolder> EXPERIMENTAL_REGISTRY_FLAGS = Map.of(
        // Update for Experimental API
        TradeRebalanceRegistries.WORLD_BUILDER, FlagHolders.TRADE_REBALANCE,
        TradeRebalanceRegistries.RELOADABLE_BUILDER, FlagHolders.TRADE_REBALANCE
    );

    private static final Multimap<ResourceKey<? extends Registry<?>>, Map.Entry<SingleFlagHolder, RegistrySetBuilder.RegistryStub>> EXPERIMENTAL_REGISTRY_ENTRIES;
    static {
        EXPERIMENTAL_REGISTRY_ENTRIES = HashMultimap.create();
        for (Map.Entry<RegistrySetBuilder, SingleFlagHolder> entry : EXPERIMENTAL_REGISTRY_FLAGS.entrySet()) {
            for (RegistrySetBuilder.RegistryStub stub : entry.getKey().entries) {
                stub.requiredRegistries().forEach(key -> EXPERIMENTAL_REGISTRY_ENTRIES.put(key, Map.entry(entry.getValue(), stub)));
            }
        }
    }

    // Builds the given stub in isolation (against the fully-built Main.REGISTRY_ACCESS as context for
    // any cross-registry lookups the bootstrap needs) and reads back the set of keys it registered.
    private static <T> Set<ResourceKey<T>> collectRegisteredKeys(RegistrySetBuilder.RegistryStub stub, ResourceKey<? extends Registry<T>> registryKey) {
        RegistrySetBuilder isolatedBuilder = new RegistrySetBuilder();
        isolatedBuilder.entries.add(stub);
        HolderLookup.Provider built = isolatedBuilder.build(Main.REGISTRY_ACCESS);
        return built.lookupOrThrow(registryKey).listElements().map(Holder.Reference::key).collect(Collectors.toSet());
    }

    public static <T> Map<ResourceKey<T>, SingleFlagHolder> collectDataDrivenElementIds(Registry<T> registry) {
        ResourceKey<? extends Registry<T>> registryKey = registry.key();
        Collection<Map.Entry<SingleFlagHolder, RegistrySetBuilder.RegistryStub>> experimentalEntries = EXPERIMENTAL_REGISTRY_ENTRIES.get(registryKey);
        if (experimentalEntries.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<ResourceKey<T>, SingleFlagHolder> result = new IdentityHashMap<>();
        for (Map.Entry<SingleFlagHolder, RegistrySetBuilder.RegistryStub> experimentalEntry : experimentalEntries) {
            Set<ResourceKey<T>> experimental = collectRegisteredKeys(experimentalEntry.getValue(), registryKey);
            result.putAll(experimental.stream().collect(Collectors.toMap(key -> key, key -> experimentalEntry.getKey())));
        }

        RegistrySetBuilder.@Nullable RegistryStub vanillaStub = VANILLA_REGISTRY_ENTRIES.get(registryKey);
        if (vanillaStub != null) {
            Set<ResourceKey<T>> vanilla = collectRegisteredKeys(vanillaStub, registryKey);
            result.keySet().removeAll(vanilla);
        }
        return result;
    }

    // collect all the tags by grabbing the json from the data-packs
    // another (probably) way is to hook into the data generator like the typed keys generator
    public static Map<TagKey<?>, String> collectTags(ResourceManager resourceManager) {
        Map<TagKey<?>, String> result = new IdentityHashMap<>();

        // collect all vanilla tags
        Multimap<ResourceKey<? extends Registry<?>>, String> vanillaTags = HashMultimap.create();
        PackResources vanillaPack = resourceManager.listPacks()
            .filter(packResources -> packResources.packId().equals(BuiltInPackSource.VANILLA_ID))
            .findFirst()
            .orElseThrow();
        collectTagsFromPack(vanillaPack, (entry, path) -> vanillaTags.put(entry.key(), path));

        // then distinct with other data-pack tags to know for sure newly created tags and so experimental one
        resourceManager.listPacks().forEach(pack -> {
            String packId = pack.packId();
            if (packId.equals(BuiltInPackSource.VANILLA_ID)) return;

            collectTagsFromPack(pack, (entry, path) -> {
                if (vanillaTags.get(entry.key()).contains(path)) {
                    return;
                }

                result.put(entry.value().listTagIds()
                    .filter(tagKey -> tagKey.location().getPath().equals(path))
                    .findFirst()
                    .orElseThrow(), packId);
            });
        });
        return Collections.unmodifiableMap(result);
    }

    private static void collectTagsFromPack(PackResources pack, BiConsumer<RegistryAccess.RegistryEntry<?>, String> output) {
        Set<String> namespaces = pack.getNamespaces(PackType.SERVER_DATA);

        for (String namespace : namespaces) {
            Main.REGISTRY_ACCESS.registries().forEach(entry -> {
                // this is probably expensive but can't find another way around and data-pack loader has similar logic
                // the issue is that registry key can have parent/key but tag key can also have parent/key so parsing become a mess
                // without having at least one of the two values
                String tagDir = Registries.tagsDirPath(entry.key());
                pack.listResources(PackType.SERVER_DATA, namespace, tagDir, (id, supplier) -> {
                    Formatting.formatTagKey(tagDir, id.getPath()).ifPresentOrElse(path -> output.accept(entry, path), () -> {
                        LOGGER.warn("Unable to parse the path: {}/{}/{}.json in the data-pack {} into a tag key", namespace, tagDir, id.getPath(), pack.packId());
                    });
                });
            });
        }
    }

    private ExperimentalCollector() {
    }
}
