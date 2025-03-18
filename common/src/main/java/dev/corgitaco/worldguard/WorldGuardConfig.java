package dev.corgitaco.worldguard;

import dev.corgitaco.worldguard.platform.ModPlatform;
import com.google.common.base.Suppliers;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

public record WorldGuardConfig(boolean throwMismatchedBiomeSource, boolean throwMismatchedChunkGenerator,
                               boolean throwMismatchedSeed, boolean throwMissingDimension,
                               boolean throwRemovedBiome) {

    public static final Supplier<Path> WORLD_GUARD_CONFIG_PATH = Suppliers.memoize(() -> ModPlatform.INSTANCE.configDir().resolve(WorldGuard.MOD_ID).resolve("config.json"));

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().setLenient().create();

    public static final Codec<WorldGuardConfig> CODEC = RecordCodecBuilder.create(
            worldGuardConfigInstance ->
                    worldGuardConfigInstance.group(
                            Codec.BOOL.fieldOf("crash_when_mismatched_biome_source").forGetter(WorldGuardConfig::throwMismatchedBiomeSource),
                            Codec.BOOL.fieldOf("crash_when_mismatched_chunk_generator").forGetter(WorldGuardConfig::throwMismatchedChunkGenerator),
                            Codec.BOOL.fieldOf("crash_when_mismatched_seed").forGetter(WorldGuardConfig::throwMismatchedSeed),
                            Codec.BOOL.fieldOf("crash_when_missing_dimension").forGetter(WorldGuardConfig::throwMissingDimension),
                            Codec.BOOL.fieldOf("crash_when_removed_biome").forGetter(WorldGuardConfig::throwRemovedBiome)
                    ).apply(worldGuardConfigInstance, WorldGuardConfig::new)
    );

    private static final WorldGuardConfig DEFAULT = new WorldGuardConfig(true, true, true, true, true);

    public static final Supplier<WorldGuardConfig> CONFIG = Suppliers.memoize(WorldGuardConfig::getConfig);

    private static WorldGuardConfig getConfig() {
        try {
            Path path = WORLD_GUARD_CONFIG_PATH.get();
            if (path.toFile().exists()) {
                return CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(Files.readString(path))).getOrThrow().getFirst();

            } else {
                if (!Files.exists(path.getParent())) {
                    Files.createDirectories(path.getParent());
                }
                Files.writeString(path, GSON.toJson(CODEC.encodeStart(JsonOps.INSTANCE, DEFAULT).getOrThrow()));
                return DEFAULT;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
