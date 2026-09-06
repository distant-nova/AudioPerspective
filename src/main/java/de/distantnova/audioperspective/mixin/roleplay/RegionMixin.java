package de.distantnova.audioperspective.mixin.roleplay;

import de.distantnova.audioperspective.VirtualListenerStore;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Applies the virtual listener to AudioPlayer Roleplay's FALL_OFF region scan.
 */
@Pseudo
@Mixin(targets = "xyz.breadloaf.audioplayerroleplay.modules.Regions.Region", remap = false)
public abstract class RegionMixin {

    @Redirect(
            method = "getPlayersWithin(ILnet/minecraft/resources/Identifier;)Ljava/util/ArrayList;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;position()Lnet/minecraft/world/phys/Vec3;",
                    remap = true
            ),
            require = 2,
            remap = false
    )
    private Vec3 audioPerspective$resolveFalloffPosition(ServerPlayer player) {
        Vec3 realPosition = player.position();
        return VirtualListenerStore.resolve(player.getUUID(), player.level().dimension(), realPosition);
    }
}
