package de.naturoforumsmp_mod;

import de.naturoforumsmp_mod.config.Configuration;
import de.naturoforumsmp_mod.config.Keybinds;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoElytra
{
    public static final int CHEST_SLOT = EquipmentSlot.CHEST.getIndex(Inventory.INVENTORY_SIZE);
    private static final Minecraft client = Minecraft.getInstance();
    public static final String MOD_ID = "naturoforumsmp_mod";
    public static final Logger logger = LoggerFactory.getLogger(MOD_ID);

    public static void initialise(String mod_name, String mod_version, String platform) {
        logger.info("{} v{} for {} successfully enabled!", mod_name, mod_version, platform);
        Configuration.load();
        Keybinds.setup();
    }

    public static void sendMessage(Component component) {
        if (client.player == null) return;
        Component message = Component.literal("[AutoElytra] ").append(component);
        client.player.displayClientMessage(message, false);
    }
}