package com.benmac.cavedifficulty;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import java.util.List;
import java.util.ArrayList;

public class Spawning {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static class SpawnEntry {
        public ResourceLocation mob;
        public int cost;

        public SpawnEntry(String entry) {
            String[] parts = entry.split(":");
            if (parts.length == 3) {
                this.mob = new ResourceLocation(parts[0], parts[1]);
                this.cost = Integer.parseInt(parts[2]);
            }
        }
    }

    private static List<SpawnEntry> spawnEntries = new ArrayList<>();
    private static boolean parsed = false;

    private static void parseSpawnPool() {
        if (!parsed) {
            for (String entry : com.benmac.cavedifficulty.Config.spawnPool) {
                spawnEntries.add(new SpawnEntry(entry));
            }
            parsed = true;
        }
    }

    private static final int MAX_MONSTERS_RADIUS = 48;
    private static final int MAX_MONSTERS = 10; // Local cap
    private static final int MAX_SPAWN_ATTEMPTS = 10;
    private static final int PACK_SIZE_MIN = 2;
    private static final int PACK_SIZE_MAX = 5;
    private static final int SPAWN_RING_MIN = 12;
    private static final int SPAWN_RING_MAX = 28;

    public static void spawnWave(ServerLevel level, BlockPos playerPos, int credits, ServerPlayer player) {
        parseSpawnPool();
        if (spawnEntries.isEmpty()) {
            return;
        }

        // Check local monster cap
        AABB area = AABB.ofSize(Vec3.atCenterOf(playerPos), MAX_MONSTERS_RADIUS * 2, MAX_MONSTERS_RADIUS * 2,
                MAX_MONSTERS_RADIUS * 2);
        List<Monster> monsters = level.getEntitiesOfClass(Monster.class, area);
        int monsterCount = monsters.size();

        RandomSource random = level.random;

        // Spend credits on spawns
        int attempts = 0;
        while (credits >= getMinCost() && attempts < 50) {
            attempts++;
            SpawnEntry entry = selectRandomEntry(credits, monsterCount, random);
            if (entry == null) {
                break;
            }

            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(entry.mob);
            if (type == null) {
                continue;
            }

            // Find spawn pos
            BlockPos spawnPos = findValidSpawnPos(level, playerPos, SPAWN_RING_MIN, SPAWN_RING_MAX, random);
            if (spawnPos == null) {
                continue;
            }

            // Create and spawn
            var entity = type.create(level);
            if (entity instanceof Monster monster) {
                monster.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                monster.finalizeSpawn((ServerLevelAccessor) level, level.getCurrentDifficultyAt(spawnPos),
                        MobSpawnType.NATURAL, null, null);
                level.addFreshEntity(monster);
                credits -= entry.cost;
            }
        }

        player.sendSystemMessage(Component.literal("A cave wave has spawned!"));
    }

    private static int getMinCost() {
        return spawnEntries.stream().mapToInt(e -> e.cost).min().orElse(10);
    }

    private static SpawnEntry selectRandomEntry(int credits, int monsterCount, RandomSource random) {
        List<SpawnEntry> affordable = spawnEntries.stream().filter(e -> e.cost <= credits).toList();
        if (affordable.isEmpty())
            return null;

        // Calculate weights: favor low-cost when few mobs, high-cost when many
        double[] weights = new double[affordable.size()];
        for (int i = 0; i < affordable.size(); i++) {
            int cost = affordable.get(i).cost;
            if (monsterCount < 10) {
                weights[i] = 1.0 / cost; // Favor cheap
            } else {
                weights[i] = cost; // Favor expensive
            }
        }

        // Select weighted random
        double totalWeight = 0;
        for (double w : weights)
            totalWeight += w;
        double rand = random.nextDouble() * totalWeight;
        double cumulative = 0;
        for (int i = 0; i < weights.length; i++) {
            cumulative += weights[i];
            if (rand <= cumulative) {
                return affordable.get(i);
            }
        }
        return affordable.get(affordable.size() - 1); // Fallback
    }

    private static BlockPos findValidSpawnPos(ServerLevel level, BlockPos center, int minDist, int maxDist,
            RandomSource random) {
        for (int attempt = 0; attempt < MAX_SPAWN_ATTEMPTS; attempt++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = minDist + random.nextDouble() * (maxDist - minDist);
            int x = center.getX() + (int) (Math.cos(angle) * distance);
            int z = center.getZ() + (int) (Math.sin(angle) * distance);
            int baseY = center.getY() + random.nextInt(9) - 4; // ±4 blocks from player Y

            // Search up/down from base Y for valid floor
            for (int dy = -5; dy <= 5; dy++) {
                BlockPos pos = new BlockPos(x, baseY + dy, z);
                if (isValidSpawnPos(level, pos)) {
                    return pos;
                }
            }
        }
        return null;
    }

    private static boolean isValidSpawnPos(ServerLevel level, BlockPos pos) {
        boolean air = level.getBlockState(pos).isAir();
        boolean solidBelow = level.getBlockState(pos.below()).isSolid();
        if (!air || !solidBelow) {
            return false;
        }
        Zombie dummy = EntityType.ZOMBIE.create(level);
        if (dummy == null) {
            return false;
        }
        dummy.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        boolean noCollision = level.noCollision(dummy.getBoundingBox());
        dummy.discard();
        return noCollision;
    }
}
