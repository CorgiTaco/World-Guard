package dev.corgitaco.worldguard.forge.datagen;

import dev.corgitaco.worldguard.WorldGuard;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = WorldGuard.MOD_ID)
class ForgeDatagen {

	@SubscribeEvent
	protected static void gatherData(final GatherDataEvent event) {

	}
}
