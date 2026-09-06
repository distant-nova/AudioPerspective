package de.distantnova.audioperspective;

import de.distantnova.audioperspective.network.VirtualListenerPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioPerspective implements ModInitializer {

    public static final String MOD_ID = "audio_perspective";
    public static final Logger LOGGER = LoggerFactory.getLogger("AudioPerspective");

    private int expirationTick;

    @Override
    public void onInitialize() {
        LOGGER.info("AudioPerspective loaded");

        PayloadTypeRegistry.serverboundPlay().register(VirtualListenerPayload.Update.TYPE, VirtualListenerPayload.Update.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(VirtualListenerPayload.Clear.TYPE, VirtualListenerPayload.Clear.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(VirtualListenerPayload.Update.TYPE, (payload, context) ->
                context.server().execute(() -> update(context.player(), payload))
        );
        ServerPlayNetworking.registerGlobalReceiver(VirtualListenerPayload.Clear.TYPE, (_, context) ->
                context.server().execute(() -> clear(context.player()))
        );
        ServerPlayConnectionEvents.DISCONNECT.register((handler, _) -> clear(handler.getPlayer()));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (++expirationTick < 20) {
                return;
            }
            expirationTick = 0;
            for (var playerId : VirtualListenerStore.expireStale()) {
                ServerPlayer player = server.getPlayerList().getPlayer(playerId);
                LOGGER.info("[AudioPerspective] expired for {}", player == null ? playerId : player.getScoreboardName());
            }
        });
    }

    private static void update(ServerPlayer player, VirtualListenerPayload.Update payload) {
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, payload.dimension());
        if (player.level().getServer().getLevel(dimension) == null) {
            LOGGER.warn("[AudioPerspective] rejected unknown dimension {} from {}", payload.dimension(), player.getScoreboardName());
            return;
        }

        Vec3 position = new Vec3(payload.x(), payload.y(), payload.z());
        if (!Double.isFinite(position.x) || !Double.isFinite(position.y) || !Double.isFinite(position.z)) {
            LOGGER.warn("[AudioPerspective] rejected non-finite position from {}", player.getScoreboardName());
            return;
        }

        VirtualListenerStore.UpdateResult result = VirtualListenerStore.update(player.getUUID(), dimension, position);
        if (result == VirtualListenerStore.UpdateResult.ENABLED || result == VirtualListenerStore.UpdateResult.MOVED) {
            LOGGER.info("[AudioPerspective] {} for {}: {} {} {} {}",
                    result == VirtualListenerStore.UpdateResult.ENABLED ? "enabled" : "moved",
                    player.getScoreboardName(),
                    payload.dimension(), position.x, position.y, position.z);
        }
    }

    private static void clear(ServerPlayer player) {
        if (VirtualListenerStore.clear(player.getUUID())) {
            LOGGER.info("[AudioPerspective] disabled for {}", player.getScoreboardName());
        }
    }
}
