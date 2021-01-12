package net.petsforever;

import java.nio.file.Path;
import java.nio.file.Paths;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.petsforever.impl.SharedPetManagerImpl;
import net.petsforever.items.PersistentWandItem;
import net.petsforever.items.SummonWandItem;
import net.petsforever.mixin.LevelStorage$SessionMixin;
import net.petsforever.mixin.MinecraftServerAccessor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.NotNull;

public class Pets implements ModInitializer{
    @NotNull public static final String MOD_ID = "petsforever";
    
    @Override
    public void onInitialize(){
        ServerLifecycleEvents.SERVER_STARTED.register(this::handleServerStart);
        ServerLifecycleEvents.SERVER_STOPPED.register(this::handleServerStop);
        Items.init();
    }
    
    private void handleServerStart(MinecraftServer server){
        Path savePath;
        LevelStorage.Session session = ((MinecraftServerAccessor)server).getSession();
        Path worldPath = ((LevelStorage$SessionMixin)session).getDirectory().getParent();
        if(server.isDedicated()){
            // To be nice to server admins
            savePath = worldPath;
        }else{
            if(Util.getOperatingSystem() == Util.OperatingSystem.WINDOWS){
                savePath = Paths.get(System.getenv("APPDATA"), ".minecraft");
            }else{
                savePath = Paths.get(System.getProperty("user.home"), ".minecraft");
            }
        }
        SharedPetManagerImpl.INSTANCE.load(savePath, worldPath);
    }
    
    private void handleServerStop(MinecraftServer server){
        SharedPetManagerImpl.INSTANCE.save();
    }
    
    public static final class Items{
        @NotNull public static final Item PERSISTENT_WAND = new PersistentWandItem(new FabricItemSettings().group(ItemGroup.MISC).maxCount(1));
        @NotNull public static final Item SUMMON_WAND = new SummonWandItem(new FabricItemSettings().group(ItemGroup.MISC).maxCount(1));
    
        private static void init(){
            register("persistent_wand", PERSISTENT_WAND);
            register("summon_wand", SUMMON_WAND);
        }
        
        private static void register(String name, Item item){
            Registry.register(Registry.ITEM, new Identifier(MOD_ID, name), item);
        }
    }
}
