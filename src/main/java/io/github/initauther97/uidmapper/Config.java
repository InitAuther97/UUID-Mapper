package io.github.initauther97.uidmapper;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.StringUtils;

@Mod.EventBusSubscriber(modid = UUIDMapper.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.ConfigValue<String> FILENAME = BUILDER
            .comment("File name of the Name-UUID map located under world folder")
            .define("mapper", "uidmapper");

    public static final ForgeConfigSpec.BooleanValue ALLOW_INTEGRATED = BUILDER
            .comment("Whether or not UUID forwarding should work on integrated server")
            .define("allow_integrated", false);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static String mapName;
    public static boolean allowIntegrated;

    private static String validateFileName()
    {
        final var name = FILENAME.get();
        if (!StringUtils.isAlphanumeric(name)) {
            final var dft = FILENAME.getDefault();
            UUIDMapper.LOGGER.error("Name is invalid: {}, using default value {}", name, dft);
            return dft;
        }
        return name;
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        mapName = validateFileName();
        allowIntegrated = ALLOW_INTEGRATED.get();
    }
}
