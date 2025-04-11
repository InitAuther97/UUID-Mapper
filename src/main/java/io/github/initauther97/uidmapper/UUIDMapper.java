package io.github.initauther97.uidmapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import io.github.initauther97.uidmapper.command.CommandRoot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(UUIDMapper.MODID)
public class UUIDMapper
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "uidmapper";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    private static UUIDMapper instance;

    public static UUIDMapper getInstance() {
        return instance;
    }

    public ConcurrentMap<String, UUID> mapping = new ConcurrentHashMap<>();
    public boolean broken = false;
    public boolean dirty = false;

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .setLenient()
            .create();

    private final String version;

    public UUIDMapper(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::serverSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        version = context.getContainer().getModInfo().getVersion().toString();
        instance = this;
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("UUIDMapper v{} made by InitAuther97", version);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Initializing on client side");
    }

    private void serverSetup(final FMLDedicatedServerSetupEvent event) {
        LOGGER.info("Initializing on dedicated server side");
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        final MinecraftServer server = event.getServer();
        if (server.isDedicatedServer()) {
            DedicatedServer dedicated = (DedicatedServer) server;
            if (dedicated.getProperties().onlineMode) {
                LOGGER.warn("Server is starting with online-mode=true, uuid redirecting won't work!");
            }
        }

        final var mapName = Config.mapName;
        LOGGER.info("Loading uuid mapping from {}", mapName);
        final var path = server.getServerDirectory().toPath().resolve(mapName + ".json");
        final var data = UidMapping.read(path, gson);
        if (data == null) {
            broken = true;
        } else {
            mapping.putAll(data);
        }
    }

    @SubscribeEvent
    public void onLevelSave(LevelEvent.Save event) {
        if (!dirty) {
            return;
        }
        final var path = event.getLevel().getServer().getServerDirectory().toPath().resolve(Config.mapName + ".json");
        if (!UidMapping.save(path, gson, Map.copyOf(mapping))) {
            event.getLevel().getServer().getPlayerList().broadcastSystemMessage(Messages.SAVE_FAILED, true);
        } else {
            event.getLevel().getServer().getPlayerList().broadcastSystemMessage(Messages.SAVE_SUCCESS, false);
        }
        dirty = true;
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        LOGGER.info("Unloading uuid mapping");
        mapping.clear();
        broken = false;
    }

    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        CommandRoot.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        final var player = event.getEntity();
        player.sendSystemMessage(Messages.INFORM_JOINER);
        final var profile = player.getGameProfile();
        if (profile.getId().equals(mapping.get(profile.getName()))) {
            player.getServer().getPlayerList()
                    .broadcastSystemMessage(Messages.informEveryone(profile), false);
        }
    }
}
