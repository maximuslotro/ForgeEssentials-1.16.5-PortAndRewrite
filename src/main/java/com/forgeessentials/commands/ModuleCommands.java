package com.forgeessentials.commands;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.forgeessentials.api.APIRegistry;
import com.forgeessentials.commands.item.CommandBind;
import com.forgeessentials.commands.item.CommandCraft;
import com.forgeessentials.commands.item.CommandDechant;
import com.forgeessentials.commands.item.CommandDrop;
import com.forgeessentials.commands.item.CommandDuplicate;
import com.forgeessentials.commands.item.CommandEnchant;
import com.forgeessentials.commands.item.CommandEnderchest;
import com.forgeessentials.commands.item.CommandKit;
import com.forgeessentials.commands.item.CommandRename;
import com.forgeessentials.commands.item.CommandRepair;
import com.forgeessentials.commands.item.CommandVirtualchest;
import com.forgeessentials.commands.player.CommandAFK;
import com.forgeessentials.commands.player.CommandBubble;
import com.forgeessentials.commands.player.CommandBurn;
import com.forgeessentials.commands.player.CommandCapabilities;
import com.forgeessentials.commands.player.CommandDoAs;
import com.forgeessentials.commands.player.CommandFly;
import com.forgeessentials.commands.player.CommandGameMode;
import com.forgeessentials.commands.player.CommandHeal;
import com.forgeessentials.commands.player.CommandInventorySee;
import com.forgeessentials.commands.player.CommandKill;
import com.forgeessentials.commands.player.CommandLocate;
import com.forgeessentials.commands.player.CommandNoClip;
import com.forgeessentials.commands.player.CommandPotion;
import com.forgeessentials.commands.player.CommandReach;
import com.forgeessentials.commands.player.CommandSeen;
import com.forgeessentials.commands.player.CommandSmite;
import com.forgeessentials.commands.player.CommandSpeed;
import com.forgeessentials.commands.player.CommandTempBan;
import com.forgeessentials.commands.player.CommandVanish;
import com.forgeessentials.commands.server.CommandDelayedAction;
import com.forgeessentials.commands.server.CommandGetCommandBook;
import com.forgeessentials.commands.server.CommandHelp;
import com.forgeessentials.commands.server.CommandModlist;
import com.forgeessentials.commands.server.CommandPing;
import com.forgeessentials.commands.server.CommandRules;
import com.forgeessentials.commands.server.CommandServerSettings;
import com.forgeessentials.commands.util.CommandsEventHandler;
import com.forgeessentials.commands.util.MobTypeLoader;
import com.forgeessentials.commands.util.ModuleCommandsEventHandler;
import com.forgeessentials.commands.world.CommandBiome;
import com.forgeessentials.commands.world.CommandButcher;
import com.forgeessentials.commands.world.CommandFindblock;
import com.forgeessentials.commands.world.CommandPregen;
import com.forgeessentials.commands.world.CommandPush;
import com.forgeessentials.commands.world.CommandRemove;
import com.forgeessentials.commands.world.CommandTime;
import com.forgeessentials.commands.world.CommandWeather;
import com.forgeessentials.core.ForgeEssentials;
import com.forgeessentials.core.misc.FECommandManager;
import com.forgeessentials.core.moduleLauncher.FEModule;
import com.forgeessentials.util.events.FEModuleEvent.FEModuleCommonSetupEvent;

@FEModule(name = "Commands", parentMod = ForgeEssentials.class)
public class ModuleCommands
{

    public static final String PERM = "fe.commands";

    public static CommandsEventHandler oldEventHandler = new CommandsEventHandler();

    public static ModuleCommandsEventHandler eventHandler = new ModuleCommandsEventHandler();

    @SubscribeEvent
    public void preLoad(FEModuleCommonSetupEvent e)
    {
        MobTypeLoader.preLoad((FMLCommonSetupEvent) e.getFMLEvent());
    }

    @SubscribeEvent
    public void load(FEModuleCommonSetupEvent event)
    {
        FECommandManager.registerCommand(new CommandTime());
        FECommandManager.registerCommand(new CommandEnchant());
        FECommandManager.registerCommand(new CommandDechant("dechant", 4, true));//TODO fix perms
        FECommandManager.registerCommand(new CommandLocate());
        FECommandManager.registerCommand(new CommandRules("rules", 0, true));
        FECommandManager.registerCommand(new CommandModlist("modlist", 0, true));
        FECommandManager.registerCommand(new CommandButcher());
        FECommandManager.registerCommand(new CommandRemove());
        FECommandManager.registerCommand(new CommandAFK());
        
        CommandKit kit = new CommandKit("kit", 0, true);
        FECommandManager.registerCommand(kit);
        MinecraftForge.EVENT_BUS.register(kit);
        
        FECommandManager.registerCommand(new CommandEnderchest("enderchest", 4, true));//TODO fix perms
        FECommandManager.registerCommand(new CommandVirtualchest());
        FECommandManager.registerCommand(new CommandCapabilities());

        CommandCraft craft = new CommandCraft("craft", 4, true);//TODO fix perms
        FECommandManager.registerCommand(craft);
        MinecraftForge.EVENT_BUS.register(craft);

        FECommandManager.registerCommand(new CommandPing("ping", 0, true));//TODO fix perms
        FECommandManager.registerCommand(new CommandInventorySee());
        FECommandManager.registerCommand(new CommandSmite());
        FECommandManager.registerCommand(new CommandBurn());
        FECommandManager.registerCommand(new CommandPotion());
        FECommandManager.registerCommand(new CommandRepair());
        FECommandManager.registerCommand(new CommandHeal());
        FECommandManager.registerCommand(new CommandKill());
        FECommandManager.registerCommand(new CommandGameMode("gamemode", 4, true));//TODO fix perms
        FECommandManager.registerCommand(new CommandDoAs("doas", 4, true));//TODO fix perms
        FECommandManager.registerCommand(new CommandServerSettings("serversettings", 4, true));//TODO fix perms
        FECommandManager.registerCommand(new CommandGetCommandBook());
        FECommandManager.registerCommand(new CommandWeather());

        CommandBind bind = new CommandBind("bind", 4, true);//TODO fix perms
        FECommandManager.registerCommand(bind);
        MinecraftForge.EVENT_BUS.register(bind);

        FECommandManager.registerCommand(new CommandRename());
        // FECommandManager.registerCommand(new CommandVanish());
        FECommandManager.registerCommand(new CommandPush());
        FECommandManager.registerCommand(new CommandDrop("drop", 4, true));//TODO fix perms
        FECommandManager.registerCommand(new CommandFindblock());
        FECommandManager.registerCommand(new CommandNoClip("noclip", 4, true));//TODO fix perms
        FECommandManager.registerCommand(new CommandBubble());
        FECommandManager.registerCommand(new CommandSpeed("speed", 4, true));//TODO fix perms
        FECommandManager.registerCommand(new CommandSeen("seen", 0, true));
        FECommandManager.registerCommand(new CommandTempBan());
        FECommandManager.registerCommand(new CommandFly("fly", 4, true));//TODO fix perms
        FECommandManager.registerCommand(new CommandHelp());
        FECommandManager.registerCommand(new CommandPregen("pregen", 4, true));//TODO fix perms
        FECommandManager.registerCommand(new CommandBiome());
        FECommandManager.registerCommand(new CommandReach("reach", 4, true));//TODO fix perms
        FECommandManager.registerCommand(new CommandVanish("vanish", 4, true));//TODO fix perms
        FECommandManager.registerCommand(new CommandDuplicate("duplicate", 4, true));//TODO fix perms
        FECommandManager.registerCommand(new CommandDelayedAction("delayedaction", 4, true));//TODO fixperms

        APIRegistry.perms.registerPermissionDescription("fe.commands", "Permission nodes for FE commands module");
    }

}
