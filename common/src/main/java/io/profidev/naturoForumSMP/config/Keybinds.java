package io.profidev.naturoForumSMP.config;

import com.mojang.blaze3d.platform.InputConstants;
import io.profidev.naturoForumSMP.NaturoForumSMP;
import io.profidev.naturoForumSMP.feature.AutoEquipController;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;

public class Keybinds {
    protected static final Map<KeyMapping, Runnable> KEYBINDS = new HashMap<>();
    public static final KeyMapping TOGGLE_AUTO_EQUIP = registerKeybind(
            new KeyMapping("key.naturoforumsmp.toggle.equip", InputConstants.Type.KEYBOARD, -1, KeyMapping.Category.INVENTORY),
            () -> {
                if (Minecraft.getInstance().gui.screen() != null) return; // Only allow when no screen is open

                boolean enabling = !Configuration.AUTO_EQUIP_ENABLED.get();

                Configuration.AUTO_EQUIP_ENABLED.set(enabling);

                Component message;
                if (enabling) {
                    AutoEquipController.resetPreviousChestItem();
                    message = Component.translatable("message.naturoforumsmp.equip.enabled").withStyle(ChatFormatting.GREEN);
                } else message = Component.translatable("message.naturoforumsmp.equip.disabled").withStyle(ChatFormatting.RED);

                NaturoForumSMP.sendMessage(message);
                Configuration.save();
            });

    private static KeyMapping registerKeybind(KeyMapping mapping, Runnable action) {
        KEYBINDS.put(mapping, action);
        return mapping;
    }

    @ExpectPlatform
    public static void setup() {
        throw new AssertionError();
    }
}