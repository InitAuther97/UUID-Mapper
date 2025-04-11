package io.github.initauther97.uidmapper.mixin;

import com.mojang.authlib.GameProfile;
import io.github.initauther97.uidmapper.UUIDMapper;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLoginPacketListenerImpl.class)
public class ServerLoginPacketListenerImplMixin {
    @Inject(method = "createFakeProfile", at = @At("HEAD"), cancellable = true)
    private void uidm$redirectFakeProfile(GameProfile p_10039_, CallbackInfoReturnable<GameProfile> cir) {
        UUIDMapper.LOGGER.info("Redirected fake profile creation");
        final var uid = UUIDMapper.getInstance().mapping.get(p_10039_.getName());
        if (uid != null) {
            cir.setReturnValue(new GameProfile(uid, p_10039_.getName()));
        }
    }
}
