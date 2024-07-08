package de.naturoforumsmp_mod.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClothConfigFactory {
    public static Screen create(Screen parent) {
        var builder = me.shedaniel.clothconfig2.api.ConfigBuilder.create()
                .setSavingRunnable(Configuration::save)
                .setTitle(Component.translatable("title.autoelytra.config"))
                .setParentScreen(parent);

        var general = builder.getOrCreateCategory(Component.empty());
        var entryBuilder = builder.entryBuilder();

        // Auto Equip Enabled
        general.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("option.autoelytra.equip.enabled"), Configuration.AUTO_EQUIP_ENABLED.get())
                .setTooltip(Component.translatable("toolip.autoelytra.equip.enabled"))
                .setDefaultValue(Configuration.AUTO_EQUIP_ENABLED.getDefault())
                .setSaveConsumer(Configuration.AUTO_EQUIP_ENABLED::set)
                .build());

        // Auto Equip Toggle Keybind
        general.addEntry(entryBuilder.fillKeybindingField(
                        Component.translatable("key.autoelytra.toggle.equip"), Keybinds.TOGGLE_AUTO_EQUIP)
                .setTooltip(Component.translatable("tooltip.autoelytra.toggle.equip"))
                .build());

        general.addEntry(entryBuilder.startDoubleField(
                Component.translatable("option.autoelytra.speed.player"), Configuration.PLAYER_SPEED.get())
                .setDefaultValue(Configuration.PLAYER_SPEED.getDefault())
                .setSaveConsumer((d) -> {
                    Configuration.PLAYER_SPEED.set(Math.max(9, Math.min(0, d)));
                })
                .build());

        general.addEntry(entryBuilder.fillKeybindingField(
                        Component.translatable("key.autoelytra.speed.up"), Keybinds.SPEED_UP)
                .setTooltip(Component.translatable("tooltip.autoelytra.speed.up"))
                .build());

        general.addEntry(entryBuilder.fillKeybindingField(
                        Component.translatable("key.autoelytra.speed.down"), Keybinds.SPEED_DOWN)
                .setTooltip(Component.translatable("tooltip.autoelytra.speed.down"))
                .build());

        return builder.build();
    }
}