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
    <T extends Entity> boolean registerPet(PetEntity<T> pet);
    <T extends Entity> Optional<T> summonEntity(PetInfo<T> info, World world);
    <T extends Entity> boolean ownsPet(PetEntity<T> pet);
}
