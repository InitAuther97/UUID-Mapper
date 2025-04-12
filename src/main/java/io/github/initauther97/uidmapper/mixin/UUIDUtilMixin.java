package io.github.initauther97.uidmapper.mixin;

import io.github.initauther97.uidmapper.UUIDMapper;
import net.minecraft.core.UUIDUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

@Mixin(value = UUIDUtil.class)
public class UUIDUtilMixin {

    /**
     * @author InitAuther97
     * @reason
     */
    @Overwrite
    public static UUID createOfflinePlayerUUID(String p_235880_) {
        UUIDMapper.LOGGER.info("Fallback redirected offline UUID creation");
        final var uid = UUIDMapper.getInstance().mapping.get(p_235880_);
        return Objects.requireNonNullElseGet(uid, () -> UUID.nameUUIDFromBytes(("OfflinePlayer:" + p_235880_).getBytes(StandardCharsets.UTF_8)));
    }
}
