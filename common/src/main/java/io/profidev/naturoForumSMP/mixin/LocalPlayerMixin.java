package io.profidev.naturoForumSMP.mixin;

import com.mojang.authlib.GameProfile;
import io.profidev.naturoForumSMP.NaturoForumSMP;
import io.profidev.naturoForumSMP.config.Configuration;
import io.profidev.naturoForumSMP.feature.AutoEquipController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {
    @Shadow @Final protected Minecraft minecraft;

    public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    // See https://github.com/NimajnebEC/auto-elytra/issues/11
    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;onClimbable()Z"))
    private boolean patchLavaFlight(LocalPlayer instance) {
        return onClimbable() || isInLava();
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/client/player/LocalPlayer;tryToStartFallFlying()Z"))
    private void tryEquipElytra(CallbackInfo ci) {
        if (!Configuration.AUTO_EQUIP_ENABLED.get()) return;
        if (!this.naturoforumsmp$canStartFlying()) return;

        // Return if a usable glider is already equipped
        if (LivingEntity.canGlideUsing(this.getItemBySlot(EquipmentSlot.CHEST), EquipmentSlot.CHEST)) return;

        // Pick the usable glider with the most durability left
        Inventory inventory = this.getInventory();
        int bestSlot = -1;
        int bestDurability = -1;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (slot == NaturoForumSMP.CHEST_SLOT) continue;
            ItemStack stack = inventory.getItem(slot);
            if (!LivingEntity.canGlideUsing(stack, EquipmentSlot.CHEST)) continue;

            int durability = stack.getMaxDamage() - stack.getDamageValue();
            if (durability > bestDurability) {
                bestSlot = slot;
                bestDurability = durability;
            }
        }
        if (bestSlot == -1) return;

        // Keep the original chest item when swapping between gliders
        if (!AutoEquipController.hasPreviousChestItem()) {
            AutoEquipController.setPreviousChestItem(this.getItemBySlot(EquipmentSlot.CHEST));
        }
        this.naturoforumsmp$swapSlots(NaturoForumSMP.CHEST_SLOT, bestSlot);
    }

    @Unique private boolean naturoforumsmp$canStartFlying() {
        return !this.onGround() && !this.isFallFlying() && !this.isInWater() && !this.hasEffect(MobEffects.LEVITATION);
    }

    @Inject(method = "aiStep", at = @At(value = "TAIL"))
    private void unequipElytra(CallbackInfo ci) {
        if (!Configuration.AUTO_EQUIP_ENABLED.get()) return;

        // Check if just stopped flying
        if (AutoEquipController.hasPreviousChestItem() && !this.isFallFlying() && this.getItemBySlot(EquipmentSlot.CHEST).has(DataComponents.GLIDER)) {
            Inventory inventory = this.getInventory();

            // Find previous chest item
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                if (AutoEquipController.matchesPreviousChestItem(inventory.getItem(slot))) {
                    this.naturoforumsmp$swapSlots(NaturoForumSMP.CHEST_SLOT, slot);
                    break;
                }
            }

            AutoEquipController.resetPreviousChestItem();
        }
    }

    @Unique private void naturoforumsmp$swapSlots(int slotA, int slotB) {
        // Convert inventory slot to menu slot
        NonNullList<Slot> slots = this.inventoryMenu.slots;
        int slotAMenu = -1;
        int slotBMenu = -1;
        for (int i = 5; i < slots.size(); i++) {  // Start at 5 to skip crafting grid
            if (slots.get(i).container != this.getInventory()) continue;
            if (slots.get(i).getContainerSlot() == slotA) slotAMenu = i;
            if (slots.get(i).getContainerSlot() == slotB) slotBMenu = i;
            if (slotAMenu > -1 && slotBMenu > -1) break;
        }

        // Slot not reachable through the inventory menu, skip instead of clicking a wrong slot
        if (slotAMenu == -1 || slotBMenu == -1 || this.minecraft.gameMode == null) return;

        // Swap using PICKUP as SWAP only works in hotbar since 1.20.4: https://github.com/NimajnebEC/auto-elytra/issues/10
        this.minecraft.gameMode.handleContainerInput(this.inventoryMenu.containerId, slotAMenu, 0, ContainerInput.PICKUP, this);
        this.minecraft.gameMode.handleContainerInput(this.inventoryMenu.containerId, slotBMenu, 0, ContainerInput.PICKUP, this);
        this.minecraft.gameMode.handleContainerInput(this.inventoryMenu.containerId, slotAMenu, 0, ContainerInput.PICKUP, this);
    }
}
