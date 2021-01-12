package net.petsforever.api;

import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface PetInfo<T extends Entity & PetEntity<T>>{
    EntityType<T> getType();
    UUID getId();
    void setSummoned(boolean summoned);
    boolean isSummoned();
    T summon(World world, BlockPos pos);
}
