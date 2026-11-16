package io.profidev.naturoForumSMP.feature;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class AutoEquipController {
    // Chest item before the elytra was equipped, without its damage so wear during flight still matches
    @Nullable
    private static ItemStack previousChestItem;

    public static void setPreviousChestItem(ItemStack item) {
        if (!item.isEmpty()) previousChestItem = withoutDamage(item);
    }

    public static boolean hasPreviousChestItem() {
        return previousChestItem != null;
    }

    public static void resetPreviousChestItem() {
        previousChestItem = null;
    }

    public static boolean matchesPreviousChestItem(ItemStack item) {
        return previousChestItem != null && !item.isEmpty() && ItemStack.isSameItemSameComponents(previousChestItem, withoutDamage(item));
    }

    private static ItemStack withoutDamage(ItemStack item) {
        ItemStack copy = item.copy();
        copy.remove(DataComponents.DAMAGE);
        return copy;
    }
}
