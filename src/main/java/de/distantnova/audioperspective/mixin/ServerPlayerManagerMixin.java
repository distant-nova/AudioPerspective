package de.distantnova.audioperspective.mixin;

import de.distantnova.audioperspective.VirtualListenerStore;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Replaces only the receiver-position operands used by SVC 2.6.22+26.2's
 * recipient range checks. It does not alter the player entity or any other caller.
 */
@Mixin(targets = "de.maxhenkel.voicechat.voice.server.ServerPlayerManager", remap = false)
public abstract class ServerPlayerManagerMixin {

    @Redirect(
            method = "getPlayersInRangeInternal(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;DLjava/util/function/Predicate;)Ljava/util/Collection;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;position()Lnet/minecraft/world/phys/Vec3;",
                    remap = true
            ),
            remap = false
    )
    private Vec3 audioPerspective$resolveThreadedPosition(ServerPlayer player) {
        return audioPerspective$resolve(player);
    }

    @Redirect(
            method = "getPlayersInRangeDirect(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;DLjava/util/function/Predicate;)Ljava/util/Collection;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;position()Lnet/minecraft/world/phys/Vec3;",
                    remap = true
            ),
            remap = false
    )
    private static Vec3 audioPerspective$resolveDirectPosition(ServerPlayer player) {
        return audioPerspective$resolve(player);
    }

    @Unique
    private static Vec3 audioPerspective$resolve(ServerPlayer player) {
        Vec3 realPosition = player.position();
        return VirtualListenerStore.resolve(player.getUUID(), player.level().dimension(), realPosition);
    }
}
