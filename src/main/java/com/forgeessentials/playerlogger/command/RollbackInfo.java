package com.forgeessentials.playerlogger.command;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import com.forgeessentials.commons.selections.Selection;
import com.forgeessentials.core.misc.Translator;
import com.forgeessentials.playerlogger.ModulePlayerLogger;
import com.forgeessentials.playerlogger.PlayerLogger;
import com.forgeessentials.playerlogger.entity.Action01Block;
import com.forgeessentials.playerlogger.entity.Action01Block.ActionBlockType;
import com.forgeessentials.util.output.ChatOutputHandler;
import com.google.common.collect.Lists;

public class RollbackInfo
{

    ServerPlayerEntity player;

    private Selection area;

    private Date time;

    List<Action01Block> changes;

    public PlaybackTask task;

    public RollbackInfo(ServerPlayerEntity player, Selection area)
    {
        this.player = player;
        this.area = area;
        this.setTime(new Date());
    }

    @SuppressWarnings("deprecation")
    public void step(int seconds)
    {
        getTime().setSeconds(getTime().getSeconds() + seconds);
    }

    public void previewChanges()
    {
        ChatOutputHandler.chatNotification(player, Translator.format("Showing changes before %s", time.toString()));

        List<Action01Block> lastChanges = changes;
        if (lastChanges == null)
            lastChanges = new ArrayList<>();

        changes = ModulePlayerLogger.getLogger().getLoggedBlockChanges(area, time, null, 0, 0);
        if (lastChanges.size() < changes.size())
        {
            for (int i = lastChanges.size(); i < changes.size(); i++)
            {
                Action01Block change = changes.get(i);
                if (change.type == ActionBlockType.PLACE)
                {
                    sendBlockChange(player, change, Blocks.AIR.defaultBlockState());
                    // System.out.println(FEConfig.FORMAT_DATE_TIME_SECONDS.format(change.time) + " REMOVED " +
                    // change.block.name);
                }
                else if (change.type == ActionBlockType.BREAK || change.type == ActionBlockType.DETONATE || change.type == ActionBlockType.BURN)
                {
                    Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(change.block.name));
                    sendBlockChange(player, change, block.stateById(change.metadata));
                    // System.out.println(FEConfig.FORMAT_DATE_TIME_SECONDS.format(change.time) + " RESTORED " +
                    // change.block.name + ":" + change.metadata);
                }
            }
        }
        else if (lastChanges.size() > changes.size())
        {
            for (int i = lastChanges.size() - 1; i >= changes.size(); i--)
            {
                Action01Block change = lastChanges.get(i);
                if (change.type == ActionBlockType.PLACE)
                {
                    Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(change.block.name));
                    sendBlockChange(player, change, block.stateById(change.metadata));
                    // System.out.println(FEConfig.FORMAT_DATE_TIME_SECONDS.format(change.time) + " REPLACED " +
                    // change.block.name);
                }
                else if (change.type == ActionBlockType.BREAK || change.type == ActionBlockType.DETONATE || change.type == ActionBlockType.BURN)
                {
                    sendBlockChange(player, change, Blocks.AIR.defaultBlockState());
                    // System.out.println(FEConfig.FORMAT_DATE_TIME_SECONDS.format(change.time) + " REBROKE " +
                    // change.block.name + ":" + change.metadata);
                }
            }
        }
    }

    public void confirm()
    {
        if (task != null)
            task.cancel();
        for (Action01Block change : changes)
        {
            if (change.type == ActionBlockType.PLACE)
            {
                ServerWorld world = DimensionManager.getWorld(change.world.id);
                world.setBlockToAir(change.getBlockPos());
                System.out.println(change.time + " REMOVED " + change.block.name);
            }
            else if (change.type == ActionBlockType.BREAK || change.type == ActionBlockType.DETONATE || change.type == ActionBlockType.BURN)
            {
                ServerWorld world = DimensionManager.getWorld(change.world.id);
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(change.block.name));
                world.setBlockState(change.getBlockPos(), block.stateById(change.metadata), 3);
                world.setTileEntity(change.getBlockPos(), PlayerLogger.blobToTileEntity(change.entity));
                System.out.println(change.time + " RESTORED " + change.block.name + ":" + change.metadata);
            }
        }
    }

    public void cancel()
    {
        if (task != null)
            task.cancel();
        for (Action01Block change : Lists.reverse(changes))
            player.connection.sendPacket(new SPacketBlockChange(DimensionManager.getWorld(change.world.id), change.getBlockPos()));
    }

    public Date getTime()
    {
        return time;
    }

    public void setTime(Date time)
    {
        this.time = time;
    }

    /**
     * Send a faked block-update to a player
     * 
     * @param player
     * @param change
     * @param newBlock
     * @param newMeta
     */
    public static void sendBlockChange(ServerPlayerEntity player, Action01Block change, BlockState newState)
    {
        SPacketBlockChange packet = new SPacketBlockChange(DimensionManager.getWorld(change.world.id), change.getBlockPos());
        packet.blockState = newState;
        player.connection.sendPacket(packet);
    }

    public static class PlaybackTask extends TimerTask
    {
        private RollbackInfo rb;
        private int speed;

        public PlaybackTask(RollbackInfo rb, int speed)
        {
            this.rb = rb;
            this.speed = speed;
        }

        @Override
        @SuppressWarnings("deprecation")
        public void run()
        {
            rb.getTime().setSeconds(rb.getTime().getSeconds() + speed);
            rb.previewChanges();
        }

    }

}