package dev.corgitaco.worldguard.neoforge.datagen;

import dev.corgitaco.worldguard.WorldGuard;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = WorldGuard.MOD_ID)
class NeoForgeDatagen {

    @SubscribeEvent
    private static void onGatherData(GatherDataEvent event) {

    }

}
