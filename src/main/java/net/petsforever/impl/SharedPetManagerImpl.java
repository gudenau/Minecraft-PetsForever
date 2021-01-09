package net.petsforever.impl;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.petsforever.api.SharedPetManager;
import net.petsforever.api.UserInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

public class SharedPetManagerImpl implements SharedPetManager{
    public static final SharedPetManagerImpl INSTANCE = new SharedPetManagerImpl();
    
    private final ReadWriteLock usersLock = new ReentrantReadWriteLock();
    private final Map<UUID, UserInfo> users = new Object2ObjectOpenHashMap<>();
    
    private Path savePath;
    private Path worldSavePath;
    private FileLock lock;
    
    //FIXME Handle exceptions
    public void load(Path path, Path worldPath){
        try{
            savePath = path.resolve("petsforever");
            worldSavePath = worldPath.resolve("petsforever");
            if(!Files.exists(savePath)){
                Files.createDirectories(savePath);
            }
            if(!Files.exists(worldSavePath)){
                Files.createDirectories(worldSavePath);
            }
            Path lockPath = savePath.resolve(".lock");
            if(!Files.exists(lockPath)){
                Files.createFile(lockPath);
            }
            FileChannel lockChannel = FileChannel.open(lockPath, StandardOpenOption.WRITE);
            lock = lockChannel.lock();
            
            usersLock.writeLock().lock();
            users.clear();
            Path usersPath = savePath.resolve("users.nbt.gz");
            if(Files.exists(usersPath)){
                try(InputStream stream = Files.newInputStream(usersPath)){
                    CompoundTag tag = NbtIo.readCompressed(stream);
                    for(String key : tag.getKeys()){
                        UUID keyId = UUID.fromString(key);
                        users.put(keyId, UserInfoImpl.fromTag(tag.getCompound(key)));
                    }
                }
            }
            Path usersWorldPath = worldSavePath.resolve("world_users.nbt.gz");
            if(Files.exists(usersWorldPath)){
                try(InputStream stream = Files.newInputStream(usersWorldPath)){
                    CompoundTag tag = NbtIo.readCompressed(stream);
                    for(String key : tag.getKeys()){
                        UUID keyId = UUID.fromString(key);
                        UserInfoImpl userInfo = (UserInfoImpl)users.get(keyId);
                        if(userInfo != null){
                            userInfo.fromWorldTag(tag.getCompound(key));
                        }
                    }
                }
            }
            usersLock.writeLock().unlock();
        }catch(IOException e){
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public void save(){
        try{
            saveUsers();
            FileChannel lockChannel = lock.channel();
            lock.release();
            lockChannel.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    
    private void saveUsers(){
        usersLock.readLock().lock();
        try(OutputStream stream = Files.newOutputStream(savePath.resolve("users.nbt.gz"), StandardOpenOption.CREATE)){
            CompoundTag tag = new CompoundTag();
            users.forEach((key, value)->
                tag.put(key.toString(), ((UserInfoImpl)value).toTag())
            );
            NbtIo.writeCompressed(tag, stream);
        }catch(IOException e){
            e.printStackTrace();
        }
        try(OutputStream stream = Files.newOutputStream(worldSavePath.resolve("world_users.nbt.gz"), StandardOpenOption.CREATE)){
            CompoundTag tag = new CompoundTag();
            users.forEach((key, value)->
                tag.put(key.toString(), ((UserInfoImpl)value).toWorldTag())
            );
            NbtIo.writeCompressed(tag, stream);
        }catch(IOException e){
            e.printStackTrace();
        }
        usersLock.readLock().unlock();
    }
    
    @Override
    public Set<UserInfo> getUsers(){
        usersLock.readLock().lock();
        Set<UserInfo> userInfo = new HashSet<>(users.values());
        usersLock.readLock().unlock();
        return userInfo;
    }
    
    @Override
    public Optional<UserInfo> getUserInfo(UUID uuid){
        if(uuid == null){
            return Optional.empty();
        }
        usersLock.readLock().lock();
        Optional<UserInfo> userInfo = Optional.ofNullable(users.get(uuid));
        usersLock.readLock().unlock();
        return userInfo;
    }
    
    @Override
    public UserInfo getUserInfo(PlayerEntity player){
        return getUserInfo(player.getUuid()).orElseGet(()->{
            usersLock.writeLock().lock();
            UserInfo info = users.computeIfAbsent(player.getUuid(), UserInfoImpl::new);
            usersLock.writeLock().unlock();
            return info;
        });
    }
}
