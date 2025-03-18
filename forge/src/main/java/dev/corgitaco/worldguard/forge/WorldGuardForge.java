package dev.corgitaco.worldguard.forge;

import dev.corgitaco.worldguard.WorldGuard;
import net.minecraftforge.fml.common.Mod;

/**
 * Main class for the mod on the Forge platform.
 */
@Mod(WorldGuard.MOD_ID)
public class WorldGuardForge {
    public WorldGuardForge() {
        WorldGuard.init();
    }
}
