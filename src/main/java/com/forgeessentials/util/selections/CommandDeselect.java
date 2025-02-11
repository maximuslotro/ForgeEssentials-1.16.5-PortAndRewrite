package com.forgeessentials.util.selections;

import net.minecraft.command.CommandException;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.permission.DefaultPermissionLevel;

import com.forgeessentials.util.PlayerInfo;
import com.forgeessentials.util.output.ChatOutputHandler;

public class CommandDeselect extends BaseCommand
{

    @Override
    public String getPrimaryAlias()
    {
        return "/desel";
    }

    @Override
    public String[] getDefaultSecondaryAliases()
    {
        return new String[] { "/deselect", "/deselect", "/sel" };
    }

    @Override
    public void processCommandPlayer(MinecraftServer server, ServerPlayerEntity sender, String[] args) throws CommandException
    {
        PlayerInfo info = PlayerInfo.get(sender.getUUID());
        info.setSel1(null);
        info.setSel2(null);
        SelectionHandler.sendUpdate(sender);
        ChatOutputHandler.chatConfirmation(sender, "Selection cleared.");
    }

    @Override
    public boolean canConsoleUseCommand()
    {
        return false;
    }

    @Override
    public String getPermissionNode()
    {
        return "fe.core.pos.deselect";
    }

    @Override
    public DefaultPermissionLevel getPermissionLevel()
    {
        return DefaultPermissionLevel.ALL;
    }
}
