package net.petsforever.impl;

import java.util.UUID;
import net.petsforever.api.PetEntity;
import net.petsforever.api.PetInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

class PetInfoImpl<T extends Entity> implements PetInfo<T>{
    private final EntityType<T> type;
    private final UUID id;
    private boolean summoned = false;
    
    @SuppressWarnings("unchecked")
    private PetInfoImpl(CompoundTag petTag){
        type = (EntityType<T>)Registry.ENTITY_TYPE.get(new Identifier(petTag.getString("type")));
        id = petTag.getUuid("id");
    }
    
    @SuppressWarnings("unchecked")
    private <E extends Entity & PetEntity<T>> PetInfoImpl(E entity){
        type = (EntityType<T>)entity.getType();
        id = entity.gud_pets$getId();
    }
    
    public static PetInfo<?> fromTag(CompoundTag petTag){
        return new PetInfoImpl<>(petTag);
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Entity> PetInfo<T> fromPet(PetEntity<T> pet){
        return new PetInfoImpl((Entity & PetEntity<T>)pet);
    }
    
    public CompoundTag toTag(){
        CompoundTag tag = new CompoundTag();
        tag.putString("type", Registry.ENTITY_TYPE.getId(type).toString());
        tag.putUuid("id", id);
        return tag;
    }
    
    @Override
    public EntityType<T> getType(){
        return type;
    }
    
    @Override
    public UUID getId(){
        return id;
    }
    
    @Override
    public void setSummoned(boolean summoned){
        this.summoned = summoned;
    }
    
    @Override
    public boolean isSummoned(){
        return summoned;
    }
    
    @Override
    public T summon(World world, BlockPos pos){
        T entity = type.create(world);
        if(entity != null){
            entity.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            world.spawnEntity(entity);
        }
        return entity;
    }
}
