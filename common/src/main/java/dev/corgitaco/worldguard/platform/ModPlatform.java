package dev.corgitaco.worldguard.platform;


import dev.corgitaco.worldguard.WorldGuard;
import dev.corgitaco.worldguard.WGModInfo;

import java.nio.file.Path;
import java.util.List;
import java.util.ServiceLoader;

public interface ModPlatform {

    ModPlatform INSTANCE = load(ModPlatform.class);

    List<WGModInfo> modInfo();

    Path configDir();

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        WorldGuard.LOGGER.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
