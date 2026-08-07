package io.papermc.paper.potion;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.BrewingRecipe;
import net.minecraft.world.item.crafting.PotionIngredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftRecipe;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.potion.PotionBrewer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class PaperPotionBrewer implements PotionBrewer {

    private final MinecraftServer minecraftServer;
    // Paper - potion mixes are now plain data-driven brewing recipes registered directly with the
    // RecipeManager (there is no longer a separate PotionBrewing registry). Track the keys added
    // through this API so resetPotionMixes() can undo exactly those, without touching other recipes.
    private final Set<ResourceKey<Recipe<?>>> addedPotionMixes = ConcurrentHashMap.newKeySet();

    public PaperPotionBrewer(final MinecraftServer minecraftServer) {
        this.minecraftServer = minecraftServer;
    }

    @Override
    @Deprecated(forRemoval = true)
    public Collection<PotionEffect> getEffects(PotionType type, boolean upgraded, boolean extended) {
        final org.bukkit.NamespacedKey key = type.getKey();

        Preconditions.checkArgument(!key.getKey().startsWith("strong_"), "Strong potion type cannot be used directly, got %s", key);
        Preconditions.checkArgument(!key.getKey().startsWith("long_"), "Extended potion type cannot be used directly, got %s", key);

        org.bukkit.NamespacedKey effectiveKey = key;
        if (upgraded) {
            effectiveKey = new org.bukkit.NamespacedKey(key.namespace(), "strong_" + key.key());
        } else if (extended) {
            effectiveKey = new org.bukkit.NamespacedKey(key.namespace(), "long_" + key.key());
        }

        final org.bukkit.potion.PotionType effectivePotionType = org.bukkit.Registry.POTION.get(effectiveKey);
        Preconditions.checkNotNull(type, "Unknown potion type from data " + effectiveKey.asMinimalString()); // Legacy error message in 1.20.4
        return effectivePotionType.getPotionEffects();
    }

    @Override
    public void addPotionMix(final PotionMix potionMix) {
        PotionIngredient input = new PotionIngredient(CraftRecipe.toIngredient(potionMix.getInput(), true), Optional.empty());
        PotionIngredient reagent = new PotionIngredient(CraftRecipe.toIngredient(potionMix.getIngredient(), true), Optional.empty());
        ItemStackTemplate output = CraftItemStack.asTemplate(potionMix.getResult());
        BrewingRecipe recipe = new BrewingRecipe(input, reagent, output);
        ResourceKey<Recipe<?>> key = CraftNamespacedKey.toResourceKey(Registries.RECIPE, potionMix.getKey());
        this.minecraftServer.getRecipeManager().addRecipe(new RecipeHolder<>(key, recipe));
        this.addedPotionMixes.add(key);
    }

    @Override
    public void removePotionMix(final NamespacedKey key) {
        ResourceKey<Recipe<?>> resourceKey = CraftNamespacedKey.toResourceKey(Registries.RECIPE, key);
        this.minecraftServer.getRecipeManager().removeRecipe(resourceKey);
        this.addedPotionMixes.remove(resourceKey);
    }

    @Override
    public void resetPotionMixes() {
        for (final ResourceKey<Recipe<?>> key : this.addedPotionMixes) {
            this.minecraftServer.getRecipeManager().removeRecipe(key);
        }
        this.addedPotionMixes.clear();
    }
}
