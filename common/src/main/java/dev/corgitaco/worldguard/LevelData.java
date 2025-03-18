package dev.corgitaco.worldguard;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.WorldGenSettings;

import java.util.List;

public record LevelData(WorldGenSettings settings, List<WGModInfo> mods, List<String> datapacks, List<String> biomeList) {
    public static final Codec<LevelData> CODEC = RecordCodecBuilder.create(
            levelDataInstance ->
                    levelDataInstance.group(
                            WorldGenSettings.CODEC.fieldOf("worldgen_settings").forGetter(LevelData::settings),
                            Codec.list(WGModInfo.CODEC).fieldOf("mod_ids").forGetter(LevelData::mods),
                            Codec.list(Codec.STRING).fieldOf("datapacks").forGetter(LevelData::datapacks),
                            Codec.list(Codec.STRING).fieldOf("biome_list").forGetter(LevelData::biomeList)
                    ).apply(levelDataInstance, LevelData::new)
    );
}
