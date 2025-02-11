package com.forgeessentials.core;

import java.io.File;
import java.nio.file.Paths;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.command.arguments.ColorArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.FileUtils;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;

import com.forgeessentials.api.APIRegistry;
import com.forgeessentials.api.UserIdent;
import com.forgeessentials.api.UserIdent.NpcUserIdent;
import com.forgeessentials.commons.BuildInfo;
import com.forgeessentials.commons.network.NetworkUtils;
import com.forgeessentials.commons.network.packets.Packet0Handshake;
import com.forgeessentials.commons.network.packets.Packet1SelectionUpdate;
import com.forgeessentials.commons.network.packets.Packet2Reach;
import com.forgeessentials.commons.network.packets.Packet3PlayerPermissions;
import com.forgeessentials.commons.network.packets.Packet5Noclip;
import com.forgeessentials.commons.network.packets.Packet6AuthLogin;
import com.forgeessentials.commons.network.packets.Packet7Remote;
import com.forgeessentials.compat.BaublesCompat;
import com.forgeessentials.compat.CompatReiMinimap;
import com.forgeessentials.compat.HelpFixer;
import com.forgeessentials.core.commands.CommandFEInfo;
import com.forgeessentials.core.commands.CommandFEWorldInfo;
import com.forgeessentials.core.commands.CommandFeReload;
import com.forgeessentials.core.commands.CommandFeSettings;
import com.forgeessentials.core.commands.CommandUuid;
import com.forgeessentials.core.commands.Arguments.FeGroupArgument;
import com.forgeessentials.core.commands.Arguments.FeIrcPlayerArgument;
import com.forgeessentials.core.config.ConfigBase;
import com.forgeessentials.core.config.ConfigData;
import com.forgeessentials.core.config.ConfigLoaderBase;
import com.forgeessentials.core.environment.Environment;
import com.forgeessentials.core.misc.BlockModListFile;
import com.forgeessentials.core.misc.FECommandManager;
import com.forgeessentials.core.misc.PermissionManager;
import com.forgeessentials.core.misc.RespawnHandler;
import com.forgeessentials.core.misc.TaskRegistry;
import com.forgeessentials.core.misc.TeleportHelper;
import com.forgeessentials.core.misc.Translator;
import com.forgeessentials.core.moduleLauncher.ModuleLauncher;
import com.forgeessentials.core.preloader.FELaunchHandler;
import com.forgeessentials.data.v2.DataManager;
import com.forgeessentials.util.DoAsCommandSender;
import com.forgeessentials.util.FEChunkLoader;
import com.forgeessentials.util.PlayerInfo;
import com.forgeessentials.util.ServerUtil;
import com.forgeessentials.util.events.FEModuleEvent;
import com.forgeessentials.util.events.FEModuleEvent.FEModuleCommonSetupEvent;
import com.forgeessentials.util.events.FEModuleEvent.FEModuleServerAboutToStartEvent;
import com.forgeessentials.util.events.FEModuleEvent.FEModuleServerStoppedEvent;
import com.forgeessentials.util.events.ForgeEssentialsEventFactory;
import com.forgeessentials.util.output.ChatOutputHandler;
import com.forgeessentials.util.output.LoggingHandler;
import com.forgeessentials.util.questioner.Questioner;
import com.forgeessentials.util.selections.CommandDeselect;
import com.forgeessentials.util.selections.CommandExpand;
import com.forgeessentials.util.selections.CommandExpandY;
import com.forgeessentials.util.selections.CommandPos;
import com.forgeessentials.util.selections.CommandWand;
import com.forgeessentials.util.selections.SelectionHandler;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

/**
 * Main mod class
 */
@Mod(ForgeEssentials.MODID)
@Mod.EventBusSubscriber(modid = ForgeEssentials.MODID, bus = Bus.MOD,value = Dist.DEDICATED_SERVER)
public class ForgeEssentials extends ConfigLoaderBase
{

    public static final String MODID = "forgeessentials";

    public static ForgeEssentials instance;

    public static Random rnd = new Random();

    /* ------------------------------------------------------------ */

    public static final String PERM = "fe";
    public static final String PERM_CORE = PERM + ".core";
    public static final String PERM_INFO = PERM_CORE + ".info";
    public static final String PERM_RELOAD = PERM_CORE + ".reload";
    public static final String PERM_VERSIONINFO = PERM_CORE + ".versioninfo";

    /* ------------------------------------------------------------ */
    /* ForgeEssentials core submodules */
    
    protected static ConfigBase configManager;
    
    protected static ModuleLauncher moduleLauncher;

    protected static TaskRegistry tasks = new TaskRegistry();

    protected static ForgeEssentialsEventFactory factory;

    protected static TeleportHelper teleportHelper;

    protected static Questioner questioner;

    protected static FECommandManager commandManager;

    /* ------------------------------------------------------------ */

    protected static File configDirectory;

    protected static boolean debugMode = false;

    protected static boolean safeMode = false;

    protected static boolean logCommandsToConsole;

    @SuppressWarnings("unused")
    private RespawnHandler respawnHandler;

    @SuppressWarnings("unused")
    private SelectionHandler selectionHandler;

    public static boolean isCubicChunksInstalled = false;

    /* ------------------------------------------------------------ */

    public ForgeEssentials()
    {
        ArgumentTypes.register("FeGroup", FeGroupArgument.class, new ArgumentSerializer<>(FeGroupArgument::group));
        ArgumentTypes.register("FeIrcPlayer", FeIrcPlayerArgument.class, new ArgumentSerializer<>(FeIrcPlayerArgument::player));
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        // new TestClass().test();
    	LoggingHandler.init();
        initConfiguration();
        BuildInfo.getBuildInfo(FELaunchHandler.getJarLocation());
        Environment.check();
        MinecraftForge.EVENT_BUS.register(this);
        
        
    }

    @SubscribeEvent
    public void preInit(FMLCommonSetupEvent event)
    {
        LoggingHandler.felog.info(String.format("Running ForgeEssentials %s (%s)", BuildInfo.getFullVersion(), BuildInfo.getBuildHash()));
        if (safeMode)
        {
            LoggingHandler.felog.warn("You are running FE in safe mode. Please only do so if requested to by the ForgeEssentials team.");
        }

        registerNetworkMessages();

        // Set up logger level
        if (debugMode)
            ((Logger) LoggingHandler.felog).setLevel(Level.DEBUG);
        else
            ((Logger) LoggingHandler.felog).setLevel(Level.INFO);

        // Register core submodules
        factory = new ForgeEssentialsEventFactory();
        teleportHelper = new TeleportHelper();
        questioner = new Questioner();
        respawnHandler = new RespawnHandler();
        selectionHandler = new SelectionHandler();
        APIRegistry.getFEEventBus().register(new CompatReiMinimap());

        // Load submodules
        moduleLauncher = new ModuleLauncher();
        moduleLauncher.preLoad(event);
    }

    @SubscribeEvent
    public void load(FMLCommonSetupEvent e)
    {
        LoggingHandler.felog
                .info(String.format("Running ForgeEssentials %s-%s (%s)", BuildInfo.getFullVersion(), BuildInfo.getBuildType(), BuildInfo.getBuildHash()));
        if (BuildInfo.isOutdated())
        {
            LoggingHandler.felog.warn("-------------------------------------------------------------------------------------");
            LoggingHandler.felog.warn(String.format("WARNING! Using ForgeEssentials build #%d, latest build is #%d", //
                    BuildInfo.getBuildNumber(), BuildInfo.getBuildNumberLatest()));
            LoggingHandler.felog.warn("We highly recommend updating asap to get the latest security and bug fixes");
            LoggingHandler.felog.warn("-------------------------------------------------------------------------------------");
        }

        isCubicChunksInstalled = ModList.get().isLoaded("cubicchunks");

        APIRegistry.getFEEventBus().post(new FEModuleCommonSetupEvent(e));
    }

    @SubscribeEvent
    public void postLoad(FMLCommonSetupEvent e)
    {
        commandManager = new FECommandManager();
    }

    /* ------------------------------------------------------------ */

    private void initConfiguration()
    {
        configManager = new ConfigBase(new File(ServerUtil.getBaseDir(), "/ForgeEssentials"));
        FileUtils.getOrCreateDirectory(FMLPaths.GAMEDIR.get().resolve("ForgeEssentials"), "ForgeEssentials");

        ConfigBase.getModuleConfig().loadModuleConfig();
        configManager.registerSpecs(configManager.getMainConfigName(), this);
        configManager.registerSpecs(configManager.getMainConfigName(), new FEConfig());
        configManager.registerSpecs(configManager.getMainConfigName(), new ChatOutputHandler());
    }

    private void registerNetworkMessages()
    {
        // Load network packages
        NetworkUtils.registerClientToServer(0, Packet0Handshake.class, Packet0Handshake::decode);
        NetworkUtils.registerServerToClient(1, Packet1SelectionUpdate.class, Packet1SelectionUpdate::decode);
		NetworkUtils.registerServerToClient(2, Packet2Reach.class, Packet2Reach::decode);
        NetworkUtils.registerServerToClient(3, Packet3PlayerPermissions.class, Packet3PlayerPermissions::decode);
        NetworkUtils.registerServerToClient(5, Packet5Noclip.class, Packet5Noclip::decode);
        NetworkUtils.registerServerToClient(7, Packet7Remote.class, Packet7Remote::decode);

    }
    
    @SubscribeEvent
    private void registerCommands(final RegisterCommandsEvent event)
    {
        FECommandManager.registerCommand(new CommandFEInfo("feinfo", 4, true));//TODO fix perms
        FECommandManager.registerCommand(new CommandFeReload("fereload", 4, true));//TODO fix perms
        FECommandManager.registerCommand(new CommandFeSettings());
        FECommandManager.registerCommand(new CommandWand());
        FECommandManager.registerCommand(new CommandUuid("uuid", 4, true));//TODO fix perms
        FECommandManager.registerCommand(new CommandFEWorldInfo("feworldinfo", 4, true));//TODO fix perms
        if (!ModuleLauncher.getModuleList().contains("WEIntegrationTools"))
        {
            FECommandManager.registerCommand(new CommandPos(1));
            FECommandManager.registerCommand(new CommandPos(2));
            FECommandManager.registerCommand(new CommandDeselect());
            FECommandManager.registerCommand(new CommandExpand());
            FECommandManager.registerCommand(new CommandExpandY());
        }
    }

    /* ------------------------------------------------------------ */

    @SubscribeEvent
    public void serverPreInit(FMLServerAboutToStartEvent e)
    {
        // Initialize data manager once server begins to start
        DataManager.setInstance(new DataManager(new File(ServerUtil.getWorldPath(), "FEData/json")));
        APIRegistry.getFEEventBus().post(new FEModuleServerAboutToStartEvent(e));
        new BaublesCompat();
    }

    @SubscribeEvent
    public void serverStarting(FMLServerStartingEvent e)
    {
        BlockModListFile.makeModList();
        BlockModListFile.dumpFMLRegistries();
        ForgeChunkManager.setForcedChunkLoadingCallback(this, new FEChunkLoader());

        ServerUtil.replaceCommand("help", new HelpFixer()); // Will be overwritten again by commands module

        registerPermissions();

        APIRegistry.getFEEventBus().post(new FEModuleEvent.FEModuleServerStartingEvent(e));
    }

    @SubscribeEvent
    public void serverStarted(FMLServerStartedEvent e)
    {
        APIRegistry.getFEEventBus().post(new FEModuleEvent.FEModuleServerStartedEvent(e));

        // TODO: what the fuck? I don't think we should just go and delete all commands colliding with ours!
        // CommandSetChecker.remove();
        FECommandManager.registerCommands();

        // Do permission registration in first server tick.
        // TODO This can be removed if the Permission API gets accepted!
        MinecraftForge.EVENT_BUS.register(new CommandPermissionRegistrationHandler());
    }

    public static final class CommandPermissionRegistrationHandler
    {
        @SubscribeEvent
        public void serverTickEvent(TickEvent.ServerTickEvent event)
        {
            PermissionManager.registerCommandPermissions();
            MinecraftForge.EVENT_BUS.unregister(this);
        }
    }

    @SubscribeEvent
    public void serverStopping(FMLServerStoppingEvent e)
    {
        APIRegistry.getFEEventBus().post(new FEModuleEvent.FEModuleServerStoppingEvent(e));
        PlayerInfo.discardAll();
    }

    @SubscribeEvent
    public void serverStopped(FMLServerStoppedEvent e)
    {
        try
        {
            APIRegistry.getFEEventBus().post(new FEModuleServerStoppedEvent(e));
            FECommandManager.clearRegisteredCommands();
            Translator.save();
        } catch (RuntimeException ex) {
            LoggingHandler.felog.fatal("Caught Runtime Exception During Server Stop event! Suppressing Fire!", ex);
        }
    }

    protected void registerPermissions()
    {
        APIRegistry.perms.registerPermission(PERM_VERSIONINFO, DefaultPermissionLevel.OP, "Shows notification to the player if FE version is outdated");

        APIRegistry.perms.registerPermission("mc.help", DefaultPermissionLevel.ALL, "Help command");

        // Teleport
        APIRegistry.perms.registerPermissionProperty(TeleportHelper.TELEPORT_COOLDOWN, "5", "Allow bypassing teleport cooldown");
        APIRegistry.perms.registerPermissionProperty(TeleportHelper.TELEPORT_WARMUP, "3", "Allow bypassing teleport warmup");
        APIRegistry.perms.registerPermissionPropertyOp(TeleportHelper.TELEPORT_COOLDOWN, "0");
        APIRegistry.perms.registerPermissionPropertyOp(TeleportHelper.TELEPORT_WARMUP, "0");
        APIRegistry.perms.registerPermission(TeleportHelper.TELEPORT_CROSSDIM_FROM, DefaultPermissionLevel.ALL, "Allow teleporting cross-dimensionally from a dimension");
        APIRegistry.perms.registerPermission(TeleportHelper.TELEPORT_CROSSDIM_TO, DefaultPermissionLevel.ALL, "Allow teleporting cross-dimensionally to a dimension");
        APIRegistry.perms.registerPermission(TeleportHelper.TELEPORT_CROSSDIM_PORTALFROM, DefaultPermissionLevel.ALL, "Allow teleporting cross-dimensionally from a dimension via a portal");
        APIRegistry.perms.registerPermission(TeleportHelper.TELEPORT_CROSSDIM_PORTALTO, DefaultPermissionLevel.ALL, "Allow teleporting cross-dimensionally to a dimension via a portal (target coordinates are origin for vanilla portals)");
        APIRegistry.perms.registerPermission(TeleportHelper.TELEPORT_FROM, DefaultPermissionLevel.ALL, "Allow being teleported from a certain location / dimension");
        APIRegistry.perms.registerPermission(TeleportHelper.TELEPORT_TO, DefaultPermissionLevel.ALL, "Allow being teleported to a certain location / dimension");
        APIRegistry.perms.registerPermission(TeleportHelper.TELEPORT_PORTALFROM, DefaultPermissionLevel.ALL, "Allow being teleported from a certain location / dimension via a portal");
        APIRegistry.perms.registerPermission(TeleportHelper.TELEPORT_PORTALTO, DefaultPermissionLevel.ALL, "Allow being teleported to a certain location / dimension via a portal");

        CommandFeSettings.addAlias("Teleport", "warmup", TeleportHelper.TELEPORT_WARMUP);
        CommandFeSettings.addAlias("Teleport", "cooldown", TeleportHelper.TELEPORT_COOLDOWN);
    }

    /* ------------------------------------------------------------ */

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void playerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.getEntity() instanceof PlayerEntity)
        {
        	PlayerEntity player = (PlayerEntity) event.getPlayer();
            UserIdent.login(player);
            PlayerInfo.login(player.getUUID());

            if (FEConfig.checkSpacesInNames)
            {
                Pattern pattern = Pattern.compile("\\s");
                Matcher matcher = pattern.matcher(player.getGameProfile().getName());
                if (matcher.find())
                {
                    String msg = Translator.format("Invalid name \"%s\" containing spaces. Please change your name!", event.getPlayer().getName());
                    Entity entity = event.getEntity();
                    if (entity instanceof ServerPlayerEntity == false) {
            			return;
            		}
            		
            		ServerPlayerEntity serverplayer = (ServerPlayerEntity)entity;
            		serverplayer.connection.disconnect(new StringTextComponent(msg));
                }
            }

            // Show version notification
            if (BuildInfo.isOutdated() && UserIdent.get(player).checkPermission(PERM_VERSIONINFO))
                ChatOutputHandler.chatWarning(player.createCommandSourceStack(),
                        String.format("ForgeEssentials build #%d outdated. Current build is #%d. Consider updating to get latest security and bug fixes.", //
                                BuildInfo.getBuildNumber(), BuildInfo.getBuildNumberLatest()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void playerLoggedOutEvent(PlayerEvent.PlayerLoggedOutEvent  event)
    {
        if (event.getEntity() instanceof PlayerEntity)
        {
            PlayerInfo.logout(event.getEntity().getUUID());
            UserIdent.logout((PlayerEntity) event.getPlayer());
        }
    }

    @SubscribeEvent
    public void playerRespawnEvent(PlayerEvent.PlayerRespawnEvent event)
    {
        if (event.getEntity() instanceof PlayerEntity)
        {
            UserIdent.get((PlayerEntity) event.getPlayer());
        }
    }

    /* ------------------------------------------------------------ */

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void commandEvent(CommandEvent event) throws CommandSyntaxException
    {
        boolean perm = false;
        try
        {
            perm = checkPerms(event.getParseResults().getContext().getCommand(), event.getParseResults().getContext().getSource().getPlayerOrException().createCommandSourceStack());
        }
        catch (CommandSyntaxException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (logCommandsToConsole)
        {
            LoggingHandler.felog.info(String.format("Player \"%s\" %s command \"/%s %s\"", event.getParseResults().getContext().getSource().getPlayerOrException().getName().getString(),
                    perm ? "used" : "tried to use", event.getParseResults().getContext().getNodes().get(0).getNode().getName(), StringUtils.join(event.getParseResults().getContext().getNodes().iterator().toString(), " ")));
        }

        if (!perm) {
            event.setCanceled(true);
            TranslationTextComponent textcomponenttranslation2 = new TranslationTextComponent("commands.generic.permission", new Object[0]);
            textcomponenttranslation2.getStyle().withColor(TextFormatting.RED);
            event.getParseResults().getContext().getSource().getPlayerOrException().sendMessage(textcomponenttranslation2, event.getParseResults().getContext().getSource().getPlayerOrException().getUUID());
        }
    }

    public boolean checkPerms(Command command, CommandSource sender) throws CommandSyntaxException {
        String node = PermissionManager.getCommandPermission(command);
        return APIRegistry.perms.checkUserPermission(UserIdent.get(sender.getPlayerOrException().getGameProfile()),node);
    }

   /* ------------------------------------------------------------ */
    
    static ForgeConfigSpec.BooleanValue FEcheckVersion;
    static ForgeConfigSpec.BooleanValue FEdebugMode;
    static ForgeConfigSpec.BooleanValue FEsafeMode;
    static ForgeConfigSpec.BooleanValue FEhideWorldEditCommands;
    static ForgeConfigSpec.BooleanValue FElogCommandsToConsole;
	
	@Override
	public void load(Builder BUILDER, boolean isReload)
    {
    	BUILDER.comment("Configure ForgeEssentials Core.").push(FEConfig.CONFIG_MAIN_CORE);
        FEcheckVersion = BUILDER.comment("Check for newer versions of ForgeEssentials on load?").define("versionCheck", true);
        //configManager.setUseCanonicalConfig(SERVER_BUILDER.comment("For modules that support it, place their configs in this file.").define("canonicalConfigs", false).get());
        FEdebugMode = BUILDER.comment("Activates developer debug mode. Spams your FML logs.")
        		.define("debug", false);
        FEsafeMode = BUILDER.comment("Activates safe mode with will ignore some errors which would normally crash the game."
        		+"Please only enable this after being instructed to do so by FE team in response to an issue on GitHub!")
        		.define("safeMode", false);
        FEhideWorldEditCommands = BUILDER.comment("Hide WorldEdit commands from /help and only show them in //help command")
        		.define("hide_worldedit_help", true);
        FElogCommandsToConsole = BUILDER.comment("Log commands to console")
        		.define("logCommands", false);
        //BuildInfo.startVersionChecks();
        BUILDER.pop();
    }
	@Override
	public void bakeConfig(boolean reload) {
    	if (reload)
            Translator.translations.clear();
        Translator.load();
        if (!FEcheckVersion.get())
            BuildInfo.checkVersion = false;
        //configManager.setUseCanonicalConfig(SERVER_BUILDER.comment("For modules that support it, place their configs in this file.").define("canonicalConfigs", false).get());
        debugMode = FEdebugMode.get();
        safeMode = FEsafeMode.get();
        HelpFixer.hideWorldEditCommands = FEhideWorldEditCommands.get();
        logCommandsToConsole = FElogCommandsToConsole.get();
        BuildInfo.startVersionChecks();
    }
	
	@Override
	public ConfigData returnData() {
		return FEConfig.data;
	}
    /* ------------------------------------------------------------ */

    public static ConfigBase getConfigManager()
    {
        return configManager;
    }

    public static File getFEDirectory()
    {
        return configDirectory;
    }

    public static boolean isDebug()
    {
        return debugMode;
    }

    public static boolean isSafeMode()
    {
        return safeMode;
    }
}
