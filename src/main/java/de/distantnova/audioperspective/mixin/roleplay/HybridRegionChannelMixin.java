package de.distantnova.audioperspective.mixin.roleplay;

import de.distantnova.audioperspective.integration.VoicechatPositionResolver;
import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Uses the virtual listener when FALL_OFF selects the nearest of multiple regions.
 */
@Pseudo
@Mixin(targets = "xyz.breadloaf.audioplayerroleplay.modules.Regions.HybridRegionChannel", remap = false)
public abstract class HybridRegionChannelMixin {

    @Redirect(
            method = "send([B)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/maxhenkel/voicechat/api/ServerPlayer;getPosition()Lde/maxhenkel/voicechat/api/Position;"
            ),
            require = 1,
            remap = false
    )
    private Position audioPerspective$resolveNearestRegionPosition(ServerPlayer player) {
        return VoicechatPositionResolver.resolve(player);
    }
}
