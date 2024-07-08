package de.naturoforumsmp_mod.feature;

import de.naturoforumsmp_mod.AutoElytra;
import de.naturoforumsmp_mod.config.Configuration;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class SpeedHack {
    public static void tick(LocalPlayer player) {
        double speedMultiplier = Configuration.PLAYER_SPEED.get();
        if (speedMultiplier == 0)
            return;
        Vec3 currentVelocity = player.getDeltaMovement();
        double lookingAngle = Math.toRadians(player.getYHeadRot());
        double x = -Math.sin(lookingAngle) * speedMultiplier;
        double z = Math.cos(lookingAngle) * speedMultiplier;
        player.setDeltaMovement(x, currentVelocity.y, z);
    }

    public static void changeSpeed(boolean up) {
        double currentSpeedValue = Configuration.PLAYER_SPEED.get();
        if (up) {
            if (currentSpeedValue < 9) {
                double incSpeed = currentSpeedValue + 1;
                Configuration.PLAYER_SPEED.set(incSpeed);
                AutoElytra.sendMessage(Component.translatable("message.autoelytra.speed.up", Math.round(incSpeed)));
            } else {
                AutoElytra.sendMessage(Component.translatable("message.autoelytra.speed.too_high"));
            }
        } else {
            if (currentSpeedValue > 0) {
                double decSpeed = currentSpeedValue - 1;
                Configuration.PLAYER_SPEED.set(decSpeed);
                AutoElytra.sendMessage(Component.translatable("message.autoelytra.speed.down", Math.round(decSpeed)));
            } else {
                AutoElytra.sendMessage(Component.translatable("message.autoelytra.speed.too_low"));
            }
        }
        Configuration.save();
    }
}
