package de.naturoforumsmp_mod.fabric;

import de.naturoforumsmp_mod.AutoElytra;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;

import java.util.Optional;

public class NaturoForumSMP implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(AutoElytra.MOD_ID);
        assert modContainer.isPresent();

        ModMetadata metadata = modContainer.get().getMetadata();
        AutoElytra.initialise(metadata.getName(), metadata.getVersion().getFriendlyString(), "fabric");
    }
}
