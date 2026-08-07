package org.bukkit.craftbukkit.inventory;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

// Paper - brewing recipes are not exposed as a dedicated Bukkit recipe type; this is a minimal
// wrapper so BrewingRecipe#toBukkitRecipe (needed by the general recipe iterator/registry) has
// something valid to return.
public class CraftBrewingRecipe implements Recipe, Keyed {

    private final NamespacedKey key;
    private final ItemStack result;

    public CraftBrewingRecipe(NamespacedKey key, ItemStack result) {
        this.key = key;
        this.result = result;
    }

    @Override
    public NamespacedKey getKey() {
        return this.key;
    }

    @Override
    public ItemStack getResult() {
        return this.result;
    }
}
