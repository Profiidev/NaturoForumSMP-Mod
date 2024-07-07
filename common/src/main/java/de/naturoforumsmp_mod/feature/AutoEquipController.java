package de.naturoforumsmp_mod.feature;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Stream;

public class AutoEquipController {
    private static final NbtPathArgument.NbtPath[] BLACKLISTED_TAGS = parseTagPaths("tag.Damage"); // List of tags to ignore when searching for previous chest item
    @Nullable
    private static Tag previousChestTag;

    public static void setPreviousChestItem(ItemStack item) {
        if (!item.isEmpty()) previousChestTag = getFilteredTag(item);
    }

    public static boolean hasPreviousChestItem() {
        return previousChestTag != null;
    }

    public static void resetPreviousChestItem() {
        previousChestTag = null;
    }

    public static boolean matchesPreviousChestItem(ItemStack item) {
        return !item.isEmpty() && getFilteredTag(item).equals(previousChestTag);
    }

    private static Tag getFilteredTag(ItemStack itemStack) {
        Tag tag = itemStack.save(new CommandBuildContext() {
            @Override
            public Stream<ResourceKey<? extends Registry<?>>> listRegistries() {
                return Stream.empty();
            }

            @Override
            public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
                return Optional.empty();
            }
        }, new CompoundTag());

        // Remove all blacklisted tags
        for (NbtPathArgument.NbtPath path : BLACKLISTED_TAGS) {
            path.remove(tag);
        }

        return tag;
    }

    private static NbtPathArgument.NbtPath[] parseTagPaths(String... args) {
        NbtPathArgument.NbtPath[] result = new NbtPathArgument.NbtPath[args.length];
        for (int i = 0; i < args.length; i++) {
            try {
                // Use data command's path parser for blacklist
                result[i] = NbtPathArgument.nbtPath().parse(new StringReader(args[i]));
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }
}