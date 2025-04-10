package dev.corgitaco.worldguard.neoforge.platform;

import dev.corgitaco.worldguard.WGModInfo;
import dev.corgitaco.worldguard.platform.ModPlatform;
import com.google.auto.service.AutoService;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.List;


@AutoService(ModPlatform.class)
public class NeoforgeModPlatform implements ModPlatform {
    @Override
    public List<WGModInfo> modInfo() {
        return ModList.get().getMods().stream().map(info -> new WGModInfo(info.getModId(), info.getVersion().getQualifier())).toList();
    }

    @Override
    public Path configDir() {
        return FMLPaths.CONFIGDIR.get();
    }
}
