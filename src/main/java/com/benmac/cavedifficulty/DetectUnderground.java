package com.benmac.cavedifficulty;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = CaveDifficulty.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DetectUnderground {
    private static final Map<UUID, Integer> TICK_COUNTERS = new HashMap<>();
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player != null && !event.player.level().isClientSide) {
            UUID uuid = event.player.getUUID();
            int count = TICK_COUNTERS.getOrDefault(uuid, 0);
            count++;
            if (count >= Config.refreshInterval) {
                var pos = event.player.blockPosition();
                var level = event.player.level();
                Holder<Biome> biome = level.getBiome(pos);
                boolean isUnderground = biome.tags()
                        .anyMatch(tag -> tag.location().equals(new ResourceLocation("forge", "is_underground")));
                CompoundTag data = event.player.getPersistentData();
                int cavePressure = data.getInt("cavepressure");
                if (isUnderground) {
                    int newPressure = cavePressure + Config.accumulationSpeed;
                    data.putInt("cavepressure", newPressure);
                    if (RANDOM.nextFloat() < (float) Config.refreshInterval / Config.waveFrequency) {
                        Spawning.spawnWave((ServerLevel) level, pos, newPressure, (ServerPlayer) event.player);
                    }
                } else {
                    data.putInt("cavepressure", Math.max(0, cavePressure - Config.accumulationSpeed * Config.decreaseMultiplier));
                }
                count = 0;
            }
            TICK_COUNTERS.put(uuid, count);
        }
    }
}
