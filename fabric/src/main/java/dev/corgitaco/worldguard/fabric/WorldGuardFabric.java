package dev.corgitaco.worldguard.fabric;

import dev.corgitaco.worldguard.WorldGuard;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

/**
 * This class is the entrypoint for the mod on the Fabric platform.
 */
public class WorldGuardFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        WorldGuard.init();
        ServerLifecycleEvents.SERVER_STARTED.register(WorldGuard::guardWorld);
    }
}
