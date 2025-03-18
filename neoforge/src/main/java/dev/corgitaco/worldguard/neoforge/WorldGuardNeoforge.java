package dev.corgitaco.worldguard.neoforge;

import dev.corgitaco.worldguard.WorldGuard;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * Main class for the mod on the NeoForge platform.
 */
@Mod(WorldGuard.MOD_ID)
public class WorldGuardNeoforge {
    public WorldGuardNeoforge(IEventBus eventBus) {
        WorldGuard.init();
    }
}
