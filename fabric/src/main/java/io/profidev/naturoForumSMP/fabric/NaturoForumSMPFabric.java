package io.profidev.naturoForumSMP.fabric;

import io.profidev.naturoForumSMP.NaturoForumSMP;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;

import java.util.Optional;

public class NaturoForumSMPFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(NaturoForumSMP.MOD_ID);
        assert modContainer.isPresent();

        ModMetadata metadata = modContainer.get().getMetadata();
        NaturoForumSMP.initialise(metadata.getName(), metadata.getVersion().getFriendlyString(), "fabric");
    }
}
