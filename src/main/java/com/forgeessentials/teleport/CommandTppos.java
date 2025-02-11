package com.forgeessentials.teleport;

import java.util.HashMap;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.server.permission.DefaultPermissionLevel;

import com.forgeessentials.commons.selections.Point;
import com.forgeessentials.commons.selections.WarpPoint;
import com.forgeessentials.core.misc.TeleportHelper;
import com.forgeessentials.core.misc.TranslatedCommandException;
import com.forgeessentials.util.ServerUtil;

public class CommandTppos extends BaseCommand
{

    /**
     * Spawn point for each dimension
     */
    public static HashMap<Integer, Point> spawnPoints = new HashMap<>();

    @Override
    public String getPrimaryAlias()
    {
        return "tppos";
    }

    @Override
    public boolean canConsoleUseCommand()
    {
        return false;
    }

    @Override
    public DefaultPermissionLevel getPermissionLevel()
    {
        return DefaultPermissionLevel.ALL;
    }

    @Override
    public String getPermissionNode()
    {
        return TeleportModule.PERM_TPPOS;
    }

    @Override
    public void processCommandPlayer(MinecraftServer server, ServerPlayerEntity sender, String[] args) throws CommandException
    {
        if (args.length == 3)
        {
            double x = parseCoordinate(sender.posX, args[0], true).getResult();
            double y = ServerUtil.parseYLocation(sender, sender.posY, args[1]);
            double z = parseCoordinate(sender.posZ, args[2], true).getResult();
            TeleportHelper.teleport(sender, new WarpPoint(sender.level, x, y, z, sender.cameraPitch,
                    sender.cameraYaw));
        }
        else
        {

        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1 || args.length == 2)
        {
            return matchToPlayers(args);
        }
        else
        {
            return null;
        }
    }

}
