package com.benmac.cavedifficulty;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = CaveDifficulty.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue DIFFICULTY_MULTIPLIER = BUILDER
        .comment("Multiplier for difficulty increase per minute in cave")
        .defineInRange("difficultyMultiplier", 1, 1, 10);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int difficultyMultiplier;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        difficultyMultiplier = DIFFICULTY_MULTIPLIER.get();
    }
}
