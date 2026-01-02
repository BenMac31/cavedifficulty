package com.benmac.cavedifficulty;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CaveDifficulty.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CaveCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("cavepressure")
                .requires(source -> source.hasPermission(4)) // Require operator
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    CompoundTag data = player.getPersistentData();
                    int pressure = data.getInt("cavepressure");
                    player.sendSystemMessage(Component.literal("Your cave pressure is: " + pressure));
                    return 1;
                }));

        dispatcher.register(Commands.literal("spawncavewave")
                .requires(source -> source.hasPermission(4)) // Require operator
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    CompoundTag data = player.getPersistentData();
                    int pressure = data.getInt("cavepressure");
                    Spawning.spawnWave(player.serverLevel(), player.blockPosition(), pressure, player);
                    return 1;
                }));
    }
}
