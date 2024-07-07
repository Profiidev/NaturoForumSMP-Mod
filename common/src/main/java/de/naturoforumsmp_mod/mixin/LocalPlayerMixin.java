package de.naturoforumsmp_mod.mixin;

import com.mojang.authlib.GameProfile;
import de.naturoforumsmp_mod.AutoElytra;
import de.naturoforumsmp_mod.feature.AutoEquipController;
import de.naturoforumsmp_mod.config.Configuration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = LocalPlayer.class)
public class LocalPlayerMixin extends AbstractClientPlayer {
    @Shadow @Final protected Minecraft minecraft;

    public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    // See https://github.com/NimajnebEC/auto-elytra/issues/11
    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;onClimbable()Z"))
    private boolean patchLavaFlight(LocalPlayer instance) {
        return onClimbable() || isInLava();
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/client/player/LocalPlayer;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"))
    private void tryEquipElytra(CallbackInfo ci) {
        if (!Configuration.AUTO_EQUIP_ENABLED.get()) return;

        if (this.autoelytra$canStartFlying()) {
            List<ItemStack> inventory = this.autoelytra$getCombinedInventory();

            // Return if elytra is already equipped
            if (inventory.get(AutoElytra.CHEST_SLOT).getItem() instanceof ElytraItem) return;

            // Find elytra in inventory
            for (int slot = 0; slot < inventory.size(); slot++) {
                ItemStack stack = inventory.get(slot);

                if (stack.getItem() instanceof ElytraItem) {
                    AutoEquipController.setPreviousChestItem(inventory.get(AutoElytra.CHEST_SLOT));
                    this.autoelytra$swapSlots(AutoElytra.CHEST_SLOT, slot);
                    return;
                }
            }
        }
    }

    @Unique private boolean autoelytra$canStartFlying() {
        return !this.onGround() && !this.isFallFlying() && !this.isInWater() && !this.hasEffect(MobEffects.LEVITATION);
    }

    @Inject(method = "aiStep", at = @At(value = "TAIL"))
    private void unequipElytra(CallbackInfo ci) {
        if (!Configuration.AUTO_EQUIP_ENABLED.get()) return;

        List<ItemStack> inventory = this.autoelytra$getCombinedInventory();

        // Check if just stopped flying
        if (AutoEquipController.hasPreviousChestItem() && !this.isFallFlying() && inventory.get(AutoElytra.CHEST_SLOT).getItem() instanceof ElytraItem) {

            // Find previous chest item
            for (int slot = 0; slot < inventory.size(); slot++) {
                if (AutoEquipController.matchesPreviousChestItem(inventory.get(slot))) {
                    this.autoelytra$swapSlots(AutoElytra.CHEST_SLOT, slot);
                    break;
                }
            }

            AutoEquipController.resetPreviousChestItem();
        }
    }

    @Unique private void autoelytra$swapSlots(int slotA, int slotB) {

        // Convert inventory slot to menu slot
        NonNullList<Slot> slots = this.inventoryMenu.slots;
        int slotAMenu = -1;
        int slotBMenu = -1;
        for (int i = 5; i < slots.size(); i++) {  // Start at 5 to skip crafting grid
            if (slots.get(i).getContainerSlot() == slotA) slotAMenu = i;
            if (slots.get(i).getContainerSlot() == slotB) slotBMenu = i;
            if (slotAMenu > -1 && slotBMenu > -1) break;
        }

        assert slotAMenu > -1;
        assert slotBMenu > -1;
        assert this.minecraft.gameMode != null;

        // Swap using ClickType.PICKUP as ClickType.SWAP only works in hotbar since 1.20.4: https://github.com/NimajnebEC/auto-elytra/issues/10
        this.minecraft.gameMode.handleInventoryMouseClick(this.inventoryMenu.containerId, slotAMenu, 0, ClickType.PICKUP, this);
        this.minecraft.gameMode.handleInventoryMouseClick(this.inventoryMenu.containerId, slotBMenu, 0, ClickType.PICKUP, this);
        this.minecraft.gameMode.handleInventoryMouseClick(this.inventoryMenu.containerId, slotAMenu, 0, ClickType.PICKUP, this);
    }

    @Unique private List<ItemStack> autoelytra$getCombinedInventory() {
        Inventory inventory = this.getInventory();

        List<ItemStack> result = new ArrayList<>();
        for (NonNullList<ItemStack> compartment : inventory.compartments) {
            result.addAll(compartment);
        }

        return result;
    }
}
