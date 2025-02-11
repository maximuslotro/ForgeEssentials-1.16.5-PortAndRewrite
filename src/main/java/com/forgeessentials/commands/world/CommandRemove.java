package com.forgeessentials.commands.world;

import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.server.permission.DefaultPermissionLevel;

import com.forgeessentials.commands.ModuleCommands;
import com.forgeessentials.commons.selections.WorldPoint;
import com.forgeessentials.core.misc.TranslatedCommandException;
import com.forgeessentials.core.misc.Translator;
import com.forgeessentials.util.output.ChatOutputHandler;

public class CommandRemove extends BaseCommand
{

    @Override
    public String getPrimaryAlias()
    {
        return "remove";
    }

    @Override
    public boolean canConsoleUseCommand()
    {
        return true;
    }

    @Override
    public DefaultPermissionLevel getPermissionLevel()
    {
        return DefaultPermissionLevel.OP;
    }

    @Override
    public String getPermissionNode()
    {
        return ModuleCommands.PERM + ".remove";
    }

    @Override
    public void processCommandPlayer(MinecraftServer server, ServerPlayerEntity sender, String[] args) throws CommandException
    {
        int radius = 10;
        double centerX;
        double centerY;
        double centerZ;

        if (args.length == 1)
        {
            radius = parseInt(args[0], 0, Integer.MAX_VALUE);
            centerX = sender.position().x;
            centerY = sender.position().y;
            centerZ = sender.position().z;
        }
        else if (args.length == 4)
        {
            radius = parseInt(args[0], 0, Integer.MAX_VALUE);
            centerX = parseDouble(args[1], sender.position().x);
            centerY = parseDouble(args[2], sender.position().y);
            centerZ = parseDouble(args[3], sender.position().z);
        }
        else
        {

        }

        List<ItemEntity> entityList = sender.world.getEntitiesWithinAABB(ItemEntity.class, new AxisAlignedBB(centerX - radius, centerY - radius, centerZ
                - radius, centerX + radius + 1, centerY + radius + 1, centerZ + radius + 1));

        int counter = 0;
        for (int i = 0; i < entityList.size(); i++)
        {
            ItemEntity entity = entityList.get(i);
            counter++;
            entity.remove();;
        }
        ChatOutputHandler.chatConfirmation(sender, Translator.format("%d items removed.", counter));
    }

    @Override
    public void processCommandConsole(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        int radius = 0;
        WorldPoint center = new WorldPoint(0, 0, 0, 0);

        if (args.length >= 4)
        {
            radius = parseInt(args[0], 0, Integer.MAX_VALUE);
            center.setX(parseInt(args[1]));
            center.setY(parseInt(args[2]));
            center.setZ(parseInt(args[3]));
            if (args.length >= 5)
            {
                center.setDimension(parseInt(args[3]));
            }
        }


        List<ItemEntity> entityList = DimensionManager.getWorld(center.getDimension()).getEntitiesWithinAABB(
                ItemEntity.class,
                new AxisAlignedBB(center.getX() - radius, center.getY() - radius, center.getZ() - radius, center.getX() + radius + 1, center.getY() + radius
                        + 1, center.getZ() + radius + 1));

        int counter = 0;
        for (int i = 0; i < entityList.size(); i++)
        {
            ItemEntity entity = entityList.get(i);
            counter++;
            entity.remove();
        }
        ChatOutputHandler.chatConfirmation(sender, Translator.format("%d items removed.", counter));
    }

}
