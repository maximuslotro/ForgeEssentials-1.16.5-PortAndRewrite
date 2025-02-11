package com.forgeessentials.core.moduleLauncher;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.objectweb.asm.Type;

import com.forgeessentials.api.APIRegistry;
import com.forgeessentials.api.APIRegistry.ForgeEssentialsRegistrar;
import com.forgeessentials.core.ForgeEssentials;
import com.forgeessentials.core.config.ConfigLoader;
import com.forgeessentials.util.events.ConfigReloadEvent;
import com.forgeessentials.util.events.FEModuleEvent.FEModuleCommonSetupEvent;
import com.forgeessentials.util.output.LoggingHandler;
import com.google.common.collect.Maps;

import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.forgespi.language.ModFileScanData;

import static net.minecraftforge.registries.ForgeRegistry.REGISTRIES;

public class ModuleLauncher
{
    public ModuleLauncher()
    {
        instance = this;
    }

    public static ModuleLauncher instance;

    private static TreeMap<String, ModuleContainer> containerMap = new TreeMap<String, ModuleContainer>();

    private static final Type MOD = Type.getType(FEModule.class);
    
    public void preLoad(FMLCommonSetupEvent e)
    {
        LoggingHandler.felog.info("Discovering and loading modules...");

        final List<ModFileScanData.AnnotationData> data = ModList.get().getAllScanData().stream()
                .map(ModFileScanData::getAnnotations)
                .flatMap(Collection::stream)
                .filter(a -> MOD.equals(a.getAnnotationType()))
                .collect(Collectors.toList());

        Map<Type, String> classModIds = Maps.newHashMap();

        // Gather all @FEModule classes
        data.stream().filter(a -> MOD.equals(a.getAnnotationType())).forEach(info -> classModIds.put(info.getClassType(), (String)info.getAnnotationData().get("value")));
        LoggingHandler.felog.info("Found {} FEModule annotations", data.size());
            
        // started ASM handling for the module loading
        //Set<ASMData> data = e.getAsmData().getAll(FEModule.class.getName());
        
        // LOAD THE MODULES!
        ModuleContainer temp, other;
        for (ModFileScanData.AnnotationData asm : data)
        {
            temp = new ModuleContainer(asm);
            if (temp.isLoadable && !APIRegistry.FE_EVENTBUS.post(new ModuleRegistrationEvent(temp)))
            {
                if (containerMap.containsKey(temp.name))
                {
                    other = containerMap.get(temp.name);
                    if (temp.doesOverride && other.mod == ForgeEssentials.instance)
                    {
                        containerMap.put(temp.name, temp);
                    }
                    else if (temp.mod == ForgeEssentials.instance && other.doesOverride)
                    {
                        continue;
                    }
                    else
                    {
                        throw new RuntimeException("{FE-Module-Launcher} " + temp.name + " is conflicting with " + other.name);
                    }
                }
                else
                {
                    containerMap.put(temp.name, temp);
                }

                temp.createAndPopulate();
                LoggingHandler.felog.debug("Discovered FE module " + temp.name);
            }
        }

        CallableMap map = new CallableMap();

       
        // Gather all @FEModule classes
        data.stream().filter(a -> Type.getType(ForgeEssentialsRegistrar.class).equals(a.getAnnotationType())).forEach(info -> classModIds.put(info.getClassType(), (String)info.getAnnotationData().get("value")));
        LoggingHandler.felog.info(REGISTRIES,"Found {} ForgeEssentialsRegistrar annotations", data.size());

        Class<?> c;
        Object obj = null;
        for (ModFileScanData.AnnotationData asm : data)
        {
            try
            {
                obj = null;
                c = Class.forName((String) asm.getAnnotationData().getClass().getName());

                try
                {
                    obj = c.getDeclaredConstructor().newInstance();
                    map.scanObject(obj);
                    // this works?? skip everything else and go on to the next one.
                    continue;
                }
                catch (Exception e1)
                {
                    // do nothing.
                }

                // if this isn't skipped.. it grabs the class, and all static methods.
                map.scanClass(c);

            }
            catch (ClassNotFoundException e1)
            {
                // nothing needed.
            }
        }

        List<ModContainer> modList = ObfuscationReflectionHelper.getPrivateValue(ModList.class, (ModList) ModList.get(), "mods");
        for (ModContainer container :modList)
            if (container.getMod() != null)
                map.scanObject(container);

        // Check modules for callables
        for (ModuleContainer module : containerMap.values())
            map.scanObject(module);

        // Register modules with configuration manager
        for (ModuleContainer module : containerMap.values())
        {
            if (module.module instanceof ConfigLoader)
            {
                LoggingHandler.felog.debug("Registering configuration for FE module " + module.name);
                ForgeEssentials.getConfigManager().registerSpecs(module.name, (ConfigLoader) module.module);
            }
            else
            {
                LoggingHandler.felog.debug("No configuration for FE module " + module.name);
            }
        }


        // TODO Check if this works
        ForgeEssentials.getConfigManager().loadAllRegisteredConfigs();
        // TODO Check if this works
        ForgeEssentials.getConfigManager().bakeAllRegisteredConfigs(false);
        APIRegistry.getFEEventBus().post(new FEModuleCommonSetupEvent(e));
    }

    public void reloadConfigs()
    {
        // TODO Check if this works
        ForgeEssentials.getConfigManager().bakeAllRegisteredConfigs(true);
        APIRegistry.getFEEventBus().post(new ConfigReloadEvent());
    }

    public void unregister(String moduleName)
    {
        ModuleContainer container = containerMap.get(moduleName);
        APIRegistry.getFEEventBus().unregister(container.module);
        containerMap.remove(moduleName);
    }

    public static Collection<String> getModuleList()
    {
        return containerMap.keySet();
    }

    public static Map<String, ModuleContainer> getModuleMap()
    {
        return containerMap;
    }

    @Nullable
    public static ModuleContainer getModuleContainer(String slug)
    {
        return containerMap.getOrDefault(slug, null);
    }
}
