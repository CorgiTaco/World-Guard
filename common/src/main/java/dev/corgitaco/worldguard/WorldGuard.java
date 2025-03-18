package dev.corgitaco.worldguard;

import dev.corgitaco.worldguard.platform.ModPlatform;
import com.google.common.base.Suppliers;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class WorldGuard {
    public static final String MOD_ID = "worldguard";


    public static final Supplier<Path> WORLD_GUARD_BACKUP_PATH = Suppliers.memoize(() -> ModPlatform.INSTANCE.configDir().resolve(MOD_ID).resolve("backup.dat"));

    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
    }

    public static void guardWorld(MinecraftServer server) {

        if (!WORLD_GUARD_BACKUP_PATH.get().toFile().exists()) {
            try {
                Files.createDirectories(WORLD_GUARD_BACKUP_PATH.get().getParent());
                NbtIo.writeCompressed(createDat(server), WORLD_GUARD_BACKUP_PATH.get());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            WorldGuardConfig worldGuardConfig = WorldGuardConfig.CONFIG.get();
            boolean throwMismatchedBiomeSource = worldGuardConfig.throwMismatchedBiomeSource();
            boolean throwMismatchedChunkGenerator = worldGuardConfig.throwMismatchedChunkGenerator();
            boolean throwMismatchedSeed = worldGuardConfig.throwMismatchedSeed();
            boolean throwMissingDimension = worldGuardConfig.throwMissingDimension();
            boolean throwMissingBiome = worldGuardConfig.throwRemovedBiome();




            try {
                CompoundTag nbt = NbtIo.readCompressed(WORLD_GUARD_BACKUP_PATH.get(), NbtAccounter.unlimitedHeap());
                RegistryOps<Tag> registryOps = server.registryAccess().createSerializationContext(NbtOps.INSTANCE);
                LevelData lastData = LevelData.CODEC.parse(registryOps, nbt).getOrThrow();
                LevelData currentData = getLevelData(server);

                // Mismatched seeds
                if (throwMismatchedSeed) {
                    if (lastData.settings().options().seed() != currentData.settings().options().seed()) {
                        throw new IllegalStateException(wrapErrorAndDumpMismatches("⛊ WORLD GUARDED: Mismatch in seed between current and last worldgen settings. Old seed: [%s] | New seed: [%s].".formatted(lastData.settings().options().seed(), currentData.settings().options().seed()), lastData, currentData));
                    }
                }

                if (throwMissingBiome) {
                    List<String> oldBiomes = lastData.biomeList();
                    List<String> newBiomes = currentData.biomeList();
                    for (String oldBiome : oldBiomes) {
                        if (!newBiomes.contains(oldBiome)) {
                            throw new IllegalStateException(wrapErrorAndDumpMismatches("⛊ WORLD GUARDED: Missing biome in current worldgen settings. Please check your installed mods and data packs.", lastData, currentData));
                        }
                    }
                }

                for (Map.Entry<ResourceKey<LevelStem>, LevelStem> dimensionEntry : lastData.settings().dimensions().dimensions().entrySet()) {
                    if (currentData.settings().dimensions().dimensions().containsKey(dimensionEntry.getKey())) {
                        LevelStem lastDimension = dimensionEntry.getValue();
                        LevelStem currentDimension = currentData.settings().dimensions().dimensions().get(dimensionEntry.getKey());
                        if (currentDimension == null) {
                            if (throwMissingDimension) {
                                throw new IllegalStateException(wrapErrorAndDumpMismatches("⛊ WORLD GUARDED: Missing dimension in current worldgen settings: " + dimensionEntry.getKey().location(), lastData, currentData));
                            }
                            continue;
                        }

                        ChunkGenerator lastGenerator = lastDimension.generator();
                        ChunkGenerator currentGenerator = currentDimension.generator();
                        if (throwMismatchedChunkGenerator) {
                            if (lastGenerator.getClass() != currentGenerator.getClass()) {
                                throw new IllegalStateException(wrapErrorAndDumpMismatches("⛊ WORLD GUARDED: Mismatch in chunk generator class between current and last worldgen settings for dimension " + dimensionEntry.getKey().location(), lastData, currentData));
                            } else {
                                MapCodec<ChunkGenerator> codec = (MapCodec<ChunkGenerator>) lastGenerator.codec();
                                BiomeSource oldBiomeSource = lastGenerator.biomeSource;
                                lastGenerator.biomeSource = currentGenerator.biomeSource;

                                Tag lastGeneratorTag = codec.encoder().encodeStart(registryOps, lastGenerator).getOrThrow();
                                Tag currentGeneratorTag = codec.encoder().encodeStart(registryOps, currentGenerator).getOrThrow();
                                if (!lastGeneratorTag.equals(currentGeneratorTag)) {
                                    throw new IllegalStateException(wrapErrorAndDumpMismatches("⛊ WORLD GUARDED: Mismatch in chunk generator settings " + dimensionEntry.getKey().location(), lastData, currentData));
                                }

                                lastGenerator.biomeSource = oldBiomeSource;
                            }
                        }


                        if (throwMismatchedBiomeSource) {
                            if (lastGenerator.getBiomeSource().getClass() != currentGenerator.getBiomeSource().getClass()) {
                                throw new IllegalStateException(wrapErrorAndDumpMismatches("⛊ WORLD GUARDED: Mismatch in biome source between current and last worldgen settings for dimension " + dimensionEntry.getKey().location(), lastData, currentData));
                            } else {
                                MapCodec<BiomeSource> codec = (MapCodec<BiomeSource>) lastGenerator.getBiomeSource().codec();
                                Tag lastGeneratorTag = codec.encoder().encodeStart(registryOps, lastGenerator.getBiomeSource()).getOrThrow();
                                Tag currentGeneratorTag = codec.encoder().encodeStart(registryOps, lastGenerator.getBiomeSource()).getOrThrow();
                                if (!lastGeneratorTag.equals(currentGeneratorTag)) {
                                    throw new IllegalStateException(wrapErrorAndDumpMismatches("⛊ WORLD GUARDED: Mismatch in biome source settings " + dimensionEntry.getKey().location(), lastData, currentData));
                                }
                            }
                        }
                    }
                }

                NbtIo.writeCompressed(createDat(server), WORLD_GUARD_BACKUP_PATH.get());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public static String wrapErrorAndDumpMismatches(String s, LevelData old, LevelData currentData) {
        StringBuilder errorMessage = new StringBuilder(s).append("\n").append("\n");
        errorMessage.append("\t\tHere are some changes World Guard detected in your pack:\n");
        errorMessage.append("\t\t==========================Detected Mod Changes==========================\n");


        Map<String, WGModInfo> oldMods = old.mods().stream().collect(Collectors.toMap(WGModInfo::modID, mod -> mod));
        Map<String, WGModInfo> newMods = currentData.mods().stream().collect(Collectors.toMap(WGModInfo::modID, mod -> mod));

        newMods.forEach((modID, wgModInfo) -> {
            WGModInfo oldModInfo = oldMods.get(modID);
            if (oldModInfo != null) {
                if (!oldModInfo.equals(wgModInfo)) {
                    errorMessage.append("\t\t\tMod \"%s\" has mismatched version from previous worldgen settings. Old: %s | New: %s\n".formatted(modID, oldModInfo.modVersion(), wgModInfo.modVersion()));
                }
            } else {
                errorMessage.append("\t\t\tMod \"%s\" was added.\n".formatted(modID));
            }
        });

        oldMods.forEach((modID, wgModInfo) -> {
            if (!newMods.containsKey(modID)) {
                errorMessage.append("\t\t\tMod \"%s\" was removed.\n".formatted(modID));
            }
        });

        errorMessage.append("\n");
        errorMessage.append("\n");
        errorMessage.append("\t\t==========================Detected Data Pack Changes==========================\n");


        List<String> oldDatapacks = old.datapacks();
        List<String> newDatapacks = currentData.datapacks();

        for (String oldDatapack : oldDatapacks) {
            if (!newDatapacks.contains(oldDatapack)) {
                errorMessage.append("\t\t\tData pack \"%s\" was removed.\n".formatted(oldDatapack));
            }
        }

        for (String newDatapack : newDatapacks) {
            if (!oldDatapacks.contains(newDatapack)) {
                errorMessage.append("\t\t\tData pack \"%s\" was added.\n".formatted(newDatapack));
            }
        }

        errorMessage.append("\n");
        errorMessage.append("\n");
        errorMessage.append("\n");

        errorMessage.append("\t\t==========================Detected Biome Changes==========================\n");
        List<String> oldBiomes = old.biomeList();
        List<String> newBiomes = currentData.biomeList();
        for (String oldBiome : oldBiomes) {
            if (!newBiomes.contains(oldBiome)) {
                errorMessage.append("\t\t\tBiome \"%s\" was removed.\n".formatted(oldBiome));
            }
        }
        for (String newBiome : newBiomes) {
            if (!oldBiomes.contains(newBiome)) {
                errorMessage.append("\t\t\tBiome %s was added.\n".formatted(newBiome));
            }
        }
        errorMessage.append("\n");
        errorMessage.append("\n");
        errorMessage.append("\n");


        errorMessage.append("\t\tIF YOU ARE CONFIDENT THIS IS A FALSE POSITIVE, YOU CAN IGNORE THIS MESSAGE. AND DELETE THE BACKUP LOCATED AT: %s\n".formatted(WORLD_GUARD_BACKUP_PATH.get().toAbsolutePath().toString()));


        return errorMessage.toString();

    }

    public static CompoundTag createDat(MinecraftServer server) throws IOException {
        LevelData input = getLevelData(server);
        Tag tag = LevelData.CODEC.encodeStart(server.registryAccess().createSerializationContext(NbtOps.INSTANCE), input).resultOrPartial(s -> LOGGER.error("Failed to encode backup level data: {}", s)).orElseThrow();
        return (CompoundTag) tag;
    }

    private static @NotNull LevelData getLevelData(MinecraftServer server) {
        RegistryAccess.Frozen registryAccess = server.registryAccess();
        WorldGenSettings worldGenSettings = new WorldGenSettings(server.getWorldData().worldGenOptions(), new WorldDimensions(registryAccess.registryOrThrow(Registries.LEVEL_STEM)));
        return new LevelData(worldGenSettings, ModPlatform.INSTANCE.modInfo(), server.getPackRepository().getSelectedPacks().stream().map(Pack::getId).toList(), server.registryAccess().registryOrThrow(Registries.BIOME).holders().map(Holder::getRegisteredName).toList());
    }

}
