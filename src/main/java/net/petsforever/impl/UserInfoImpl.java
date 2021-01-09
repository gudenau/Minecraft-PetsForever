package net.petsforever.impl;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.fabricmc.fabric.api.util.NbtType;
import net.petsforever.api.PetEntity;
import net.petsforever.api.PetInfo;
import net.petsforever.api.UserInfo;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.World;

class UserInfoImpl implements UserInfo{
    private final UUID id;
    private final Map<UUID, PetInfo<?>> pets = new Object2ObjectOpenHashMap<>();
    private final ReadWriteLock petsLock = new ReentrantReadWriteLock();
    
    UserInfoImpl(UUID id){
        this.id = id;
    }
    
    private UserInfoImpl(CompoundTag tag){
        this.id = tag.getUuid("id");
        for(Tag rawPetTag : tag.getList("pets", NbtType.LIST)){
            CompoundTag petTag = (CompoundTag)rawPetTag;
            PetInfo<?> petInfo = PetInfoImpl.fromTag(petTag);
            pets.put(petInfo.getId(), petInfo);
        }
    }
    
    public static UserInfo fromTag(CompoundTag compound){
        return new UserInfoImpl(compound);
    }
    
    public CompoundTag toTag(){
        CompoundTag tag =  new CompoundTag();
        tag.putUuid("id", id);
        petsLock.readLock().lock();
        ListTag petsTag = new ListTag();
        for(PetInfo<?> pet : pets.values()){
            petsTag.add(((PetInfoImpl<?>)pet).toTag());
        }
        tag.put("pets", petsTag);
        petsLock.readLock().unlock();
        return tag;
    }
    
    @Override
    public UUID getOwnerId(){
        return id;
    }
    
    @Override
    public Set<PetInfo<?>> getPetInfo(){
        petsLock.readLock().lock();
        Set<PetInfo<?>> pets = new HashSet<>(this.pets.values());
        petsLock.readLock().unlock();
        return pets;
    }
    
    @Override
    public Optional<PetInfo<?>> getPetInfo(UUID petId){
        petsLock.readLock().lock();
        Optional<PetInfo<?>> info = Optional.ofNullable(pets.get(petId));
        petsLock.readLock().unlock();
        return info;
    }
    
    @Override
    public int getPetLimit(){
        return 16;
    }
    
    @Override
    public int getPetCount(){
        petsLock.readLock().lock();
        int petCount = pets.size();
        petsLock.readLock().unlock();
        return petCount;
    }
    
    @Override
    public <T extends Entity> boolean registerPet(PetEntity<T> pet){
        petsLock.writeLock().lock();
        try{
            if(pets.size() >= getPetLimit()){
                return false;
            }
    
            if(!pet.gud_pets$getOwnerId().equals(id)){
                return false;
            }
            pets.put(pet.gud_pets$getId(), PetInfoImpl.fromPet(pet));
    
            return true;
        }finally{
            petsLock.writeLock().unlock();
        }
    }
    
    @Override
    public <T extends Entity> Optional<T> summonEntity(PetInfo<T> info, World world){
        return Optional.empty();
    }
    
    @Override
    public <T extends Entity> boolean ownsPet(PetEntity<T> pet){
        petsLock.readLock().lock();
        boolean result = pets.containsKey(pet.gud_pets$getId());
        petsLock.readLock().unlock();
        return result;
    }
    
    public void fromWorldTag(CompoundTag tag){
        petsLock.writeLock().lock();
        for(Tag rawTag : tag.getList("pets", NbtType.COMPOUND)){
            CompoundTag petTag = (CompoundTag)rawTag;
            if(petTag.getBoolean("summoned")){
                getPetInfo(tag.getUuid("id"))
                    .ifPresent((info)->info.setSummoned(true));
            }
        }
        petsLock.writeLock().unlock();
    }
    
    public CompoundTag toWorldTag(){
        CompoundTag tag = new CompoundTag();
        petsLock.readLock().lock();
        ListTag list = new ListTag();
        pets.forEach((id, pet)->{
            CompoundTag petTag = new CompoundTag();
            petTag.putUuid("id", pet.getId());
            petTag.putBoolean("summoned", pet.isSummoned());
            list.add(petTag);
        });
        petsLock.readLock().unlock();
        tag.put("pets", list);
        return tag;
    }
}
