package de.distantnova.audioperspective.mixin.roleplay;

import de.distantnova.audioperspective.integration.VoicechatPositionResolver;
import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Applies the virtual listener to AudioPlayer Roleplay's CLIP region predicate.
 */
@Pseudo
@Mixin(targets = "xyz.breadloaf.audioplayerroleplay.modules.Regions.RegionHooks", remap = false)
public abstract class RegionHooksMixin {

    @Redirect(
            method = "lambda$onPostPlay$0(Lxyz/breadloaf/audioplayerroleplay/modules/Regions/RegionDataModule;Lde/maxhenkel/voicechat/api/ServerPlayer;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/maxhenkel/voicechat/api/ServerPlayer;getPosition()Lde/maxhenkel/voicechat/api/Position;"
            ),
            remap = false
    )
    private static Position audioPerspective$resolveClipPosition(ServerPlayer player) {
        return VoicechatPositionResolver.resolve(player);
    }
}
