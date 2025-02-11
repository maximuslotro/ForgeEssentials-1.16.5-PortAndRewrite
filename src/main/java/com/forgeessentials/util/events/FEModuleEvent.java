package com.forgeessentials.util.events;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.event.server.ServerLifecycleEvent;
import net.minecraftforge.eventbus.api.Event;

public class FEModuleEvent extends Event
{

    protected ServerLifecycleEvent event;
    protected FMLCommonSetupEvent event2;

    public FMLCommonSetupEvent getFMLEvent()
    {
        return event2;
    }

    public static class FEModuleCommonSetupEvent extends FEModuleEvent
    {
        public FEModuleCommonSetupEvent(FMLCommonSetupEvent event)
        {
            this.event2 = event;
        }
    }

    public static class FEModuleServerAboutToStartEvent extends FEModuleEvent
    {
        public FEModuleServerAboutToStartEvent(FMLServerAboutToStartEvent event)
        {
            this.event = event;
        }
    }

    public static class FEModuleServerStartingEvent extends FEModuleEvent
    {
        public FEModuleServerStartingEvent(FMLServerStartingEvent event)
        {
            this.event = event;
        }
    }

    public static class FEModuleServerStartedEvent extends FEModuleEvent
    {
        public FEModuleServerStartedEvent(FMLServerStartedEvent event)
        {
            this.event = event;
        }
    }

    public static class FEModuleServerStoppingEvent extends FEModuleEvent
    {
        public FEModuleServerStoppingEvent(FMLServerStoppingEvent event)
        {
            this.event = event;
        }
    }

    public static class FEModuleServerStoppedEvent extends FEModuleEvent
    {
        public FEModuleServerStoppedEvent(FMLServerStoppedEvent event)
        {
            this.event = event;
        }
    }

}
