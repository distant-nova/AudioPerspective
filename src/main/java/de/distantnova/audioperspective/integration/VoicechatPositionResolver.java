package de.distantnova.audioperspective.integration;

import de.distantnova.audioperspective.VirtualListenerStore;
import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class VoicechatPositionResolver {

    private VoicechatPositionResolver() {
    }

    public static Position resolve(ServerPlayer player) {
        Position fallback = player.getPosition();
        if (!(player.getPlayer() instanceof net.minecraft.server.level.ServerPlayer minecraftPlayer)) {
            return fallback;
        }

        Vec3 realPosition = new Vec3(fallback.getX(), fallback.getY(), fallback.getZ());
        Vec3 resolved = VirtualListenerStore.resolve(
                player.getUuid(), minecraftPlayer.level().dimension(), realPosition
        );
        if (resolved == realPosition) {
            return fallback;
        }
        return new ResolvedPosition(resolved.x, resolved.y, resolved.z);
    }

    private record ResolvedPosition(double x, double y, double z) implements Position {

        @Override
        public double getX() {
            return x;
        }

        @Override
        public double getY() {
            return y;
        }

        @Override
        public double getZ() {
            return z;
        }
    }
}
