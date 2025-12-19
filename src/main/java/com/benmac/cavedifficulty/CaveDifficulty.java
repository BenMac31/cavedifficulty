package com.benmac.cavedifficulty;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.slf4j.Logger;



/**
 * CaveDifficulty — Makes it so the longer you are in a cave the harder the game gets.
 */
@Mod(CaveDifficulty.MODID)
public class CaveDifficulty {
    public static final String MODID = "cavedifficulty";
    private static final Logger LOGGER = LogUtils.getLogger();


    public CaveDifficulty() {
        LOGGER.info("CAVEDIFFICULTY: mod constructor ran");

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("CAVEDIFFICULTY: Common setup active");
    }

    // ------------------------------------------------------------------------
    // Client setup (unchanged)
    // ------------------------------------------------------------------------
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Client-only setup
        }
    }

    // ------------------------------------------------------------------------
    // Forge events
    // ------------------------------------------------------------------------
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase == TickEvent.Phase.END && event.player != null && !event.player.level().isClientSide) {
            }
        }
    }
}
