package net.petsforever.api;

import java.util.UUID;
import net.minecraft.entity.Entity;

// This is basically a duck, make sure names are unique.
public interface PetEntity<T extends Entity>{
    UUID gud_pets$getOwnerId();
    UUID gud_pets$getId();
}
