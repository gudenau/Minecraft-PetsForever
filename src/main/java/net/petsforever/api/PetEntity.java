package net.petsforever.api;

import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;

// This is basically a duck, make sure names are unique.
public interface PetEntity<T extends Entity>{
    UUID petsforever$getOwnerId();
    UUID petsforever$getId();
    CompoundTag petsforever$toTag();
    void petsforever$fromTag(CompoundTag tag);
}
