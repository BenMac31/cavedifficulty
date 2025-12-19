package com.benmac.cavedifficulty;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = CaveDifficulty.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue DIFFICULTY_MULTIPLIER = BUILDER
        .comment("Multiplier for difficulty increase per minute in cave")
        .defineInRange("difficultyMultiplier", 1, 1, 10);

    private static final ForgeConfigSpec.IntValue REFRESH_INTERVAL = BUILDER
        .comment("Ticks between cave pressure checks")
        .defineInRange("refreshInterval", 200, 1, 72000); // 1 tick to 1 hour

    private static final ForgeConfigSpec.IntValue ACCUMULATION_SPEED = BUILDER
        .comment("Cave pressure change per refresh interval")
        .defineInRange("accumulationSpeed", 30, 1, 1000);

    private static final ForgeConfigSpec.IntValue DECREASE_MULTIPLIER = BUILDER
        .comment("Multiplier for how much faster cave pressure decreases than increases")
        .defineInRange("decreaseMultiplier", 3, 1, 10);

    private static final ForgeConfigSpec.IntValue WAVE_FREQUENCY = BUILDER
        .comment("Average ticks between cave waves (used as percent chance per refresh)")
        .defineInRange("waveFrequency", 2400, 1, 72000); // 1 tick to 1 hour

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SPAWN_POOL = BUILDER
        .comment("List of spawn entries: 'modid:entity:cost' e.g. 'minecraft:zombie:10'")
        .defineList("spawnPool", Arrays.asList("minecraft:zombie:10", "minecraft:skeleton:25", "minecraft:creeper:50"), s -> s instanceof String);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int difficultyMultiplier;
    public static int refreshInterval;
    public static int accumulationSpeed;
    public static int decreaseMultiplier;
    public static int waveFrequency;
    public static List<String> spawnPool;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        difficultyMultiplier = DIFFICULTY_MULTIPLIER.get();
        refreshInterval = REFRESH_INTERVAL.get();
        accumulationSpeed = ACCUMULATION_SPEED.get();
        decreaseMultiplier = DECREASE_MULTIPLIER.get();
        waveFrequency = WAVE_FREQUENCY.get();
        spawnPool = new ArrayList<>(SPAWN_POOL.get());
    }
}
