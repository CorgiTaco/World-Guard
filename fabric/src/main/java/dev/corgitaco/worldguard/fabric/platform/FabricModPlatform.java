package dev.corgitaco.worldguard.fabric.platform;

import dev.corgitaco.worldguard.WGModInfo;
import dev.corgitaco.worldguard.platform.ModPlatform;
import com.google.auto.service.AutoService;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.List;

@AutoService(ModPlatform.class)
public class FabricModPlatform implements ModPlatform {
    @Override
    public List<WGModInfo> modInfo() {
        return FabricLoader.getInstance().getAllMods().stream().map(modContainer -> new WGModInfo(modContainer.getMetadata().getId(), modContainer.getMetadata().getVersion().getFriendlyString())).toList();
    }

    @Override
    public Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
