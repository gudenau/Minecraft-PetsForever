package net.petsforever.api;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public interface UserInfo{
    UUID getOwnerId();
    Set<PetInfo<?>> getPetInfo();
    Optional<PetInfo<?>> getPetInfo(UUID petId);
    int getPetLimit();
    int getPetCount();
    
    @SuppressWarnings("unchecked")
    default <T extends Entity & PetEntity<T>> boolean registerPet(Entity entity){
        if(!(entity instanceof PetEntity<?>)){
            return false;
        }
        return registerPet((PetEntity<T>)entity);
    }
    
    @SuppressWarnings("unchecked")
    default <T extends Entity & PetEntity<T>> boolean ownsPet(Entity entity){
        if(!(entity instanceof PetEntity<?>)){
            return false;
        }
        return ownsPet((PetEntity<T>)entity);
    }
    
    <T extends Entity & PetEntity<T>> boolean registerPet(PetEntity<T> pet);
    <T extends Entity & PetEntity<T>> Optional<T> summonEntity(PetInfo<T> info, World world);
    <T extends Entity & PetEntity<T>> boolean ownsPet(PetEntity<T> pet);
}
