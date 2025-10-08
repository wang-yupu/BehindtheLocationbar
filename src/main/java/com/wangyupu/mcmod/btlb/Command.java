package com.wangyupu.mcmod.btlb;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3i;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import java.util.Map;
import java.util.UUID;

import com.mojang.datafixers.util.Either;
import com.wangyupu.mcmod.btlb.waypoint.Waypoint;

public class Command {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    public static void initCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    literal("lb")
                            .executes(context -> {
                                sendClientMessage("type `lb loc` to location.");
                                return 1;
                            })
                            .then(literal("loc").executes(context -> {
                                Vec3i ppos = new Vec3i(0, 0, 0);
                                if (client.player != null) {
                                    ppos = new Vec3i((int) client.player.getPos().x, (int) client.player.getPos().y,
                                            (int) client.player.getPos().z);
                                } else {
                                    return 0;
                                }

                                WaypointDataProvider.getInstance().estimateAll();
                                sendClientMessage("Estimate all successful");
                                for (Map.Entry<Either<UUID, String>, Waypoint> entry : WaypointDataProvider
                                        .getInstance().getFinalResult().entrySet()) {
                                    if (entry.getValue().pos != null) {
                                        sendClientMessage(
                                                "[" + getPlayerName(entry.getKey()) + "]: "
                                                        + entry.getValue().pos.toShortString() + " D: "
                                                        + Math.round(Math
                                                                .sqrt(ppos.getSquaredDistance(entry.getValue().pos)))
                                                        + "m"
                                                        + " (" + entry.getValue().status.toString() + ")");
                                    } else {
                                        sendClientMessage(
                                                "[" + getPlayerName(entry.getKey()) + "]: " + "N/A");
                                    }
                                }
                                sendClientMessage("Your pos: " + ppos.toShortString());
                                return 1;
                            }))
                            .then(literal("reset").executes(context -> {
                                WaypointDataProvider.getInstance().reset();
                                sendClientMessage("Resetted");
                                return 1;
                            })));
        });
    }

    private static String getPlayerName(Either<UUID, String> v) {
        if (v.right().isPresent()) {
            return v.right().get();
        } else {
            UUID id = v.left().orElse(null);
            if (id == null)
                return "N/A";

            var entry = client.getNetworkHandler().getPlayerListEntry(id);
            if (entry == null) {
                return "N/A";
            }

            return entry.getProfile().getName();
        }
    }

    private static void sendClientMessage(String message) {
        if (client != null && client.player != null) {
            client.player.sendMessage(Text.literal(message), false);
        }
    }
}
