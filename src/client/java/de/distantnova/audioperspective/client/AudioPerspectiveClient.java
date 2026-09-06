package de.distantnova.audioperspective.client;

import de.distantnova.audioperspective.network.VirtualListenerPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class AudioPerspectiveClient implements ClientModInitializer {

    private static final int UPDATE_INTERVAL_TICKS = 4;

    private boolean wasDetached;
    private int ticksUntilUpdate;
    private ResourceKey<Level> lastDimension;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> resetLocalState());
    }

    private void onEndTick(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.level == null) {
            if (wasDetached && minecraft.getConnection() != null) {
                sendClear();
            }
            resetLocalState();
            return;
        }

        Entity camera = minecraft.getCameraEntity();
        boolean detached = camera != null && camera != minecraft.player;

        if (detached) {
            ResourceKey<Level> dimension = minecraft.level.dimension();
            if (wasDetached && lastDimension != null && !lastDimension.equals(dimension)) {
                sendClear();
                ticksUntilUpdate = 0;
            }

            if (!wasDetached || ticksUntilUpdate-- <= 0) {
                Vec3 position = camera.position();
                if (ClientPlayNetworking.canSend(VirtualListenerPayload.Update.TYPE)) {
                    ClientPlayNetworking.send(new VirtualListenerPayload.Update(
                            dimension.identifier(), position.x, position.y, position.z
                    ));
                }
                ticksUntilUpdate = UPDATE_INTERVAL_TICKS - 1;
            }
            lastDimension = dimension;
        } else if (wasDetached) {
            sendClear();
            lastDimension = null;
        }

        wasDetached = detached;
    }

    private static void sendClear() {
        if (ClientPlayNetworking.canSend(VirtualListenerPayload.Clear.TYPE)) {
            ClientPlayNetworking.send(VirtualListenerPayload.Clear.INSTANCE);
        }
    }

    private void resetLocalState() {
        wasDetached = false;
        ticksUntilUpdate = 0;
        lastDimension = null;
    }
}
