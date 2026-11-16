package io.profidev.naturoForumSMP.config.fabric;

import io.profidev.naturoForumSMP.config.Keybinds;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.Map;

public class KeybindsImpl extends Keybinds {
    public static void handleKeybinds(Minecraft client) {
        for (Map.Entry<KeyMapping, Runnable> binding : KEYBINDS.entrySet()) {
            while (binding.getKey().consumeClick()) binding.getValue().run();
        }
    }

    public static void setup() {
        // Register handler
        ClientTickEvents.END_CLIENT_TICK.register(KeybindsImpl::handleKeybinds);

        // Register mappings
        for (Map.Entry<KeyMapping, Runnable> binding : KEYBINDS.entrySet()) {
            KeyMappingHelper.registerKeyMapping(binding.getKey());
        }
    }
}