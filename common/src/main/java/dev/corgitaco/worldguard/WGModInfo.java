package dev.corgitaco.worldguard;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record WGModInfo(String modID, String modVersion) {

    public static final Codec<WGModInfo> CODEC = RecordCodecBuilder.create(
            wgModInfoInstance ->
                    wgModInfoInstance.group(
                            Codec.STRING.fieldOf("modID").forGetter(WGModInfo::modID),
                            Codec.STRING.fieldOf("modVersion").forGetter(WGModInfo::modVersion)
                    ).apply(wgModInfoInstance, WGModInfo::new)
    );
}
