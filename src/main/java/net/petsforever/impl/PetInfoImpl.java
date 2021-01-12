package net.petsforever.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import net.minecraft.nbt.NbtIo;
import net.petsforever.api.PetEntity;
import net.petsforever.api.PetInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

class PetInfoImpl<T extends Entity & PetEntity<T>> implements PetInfo<T>{
    private final Path savePath;
    private final EntityType<T> type;
    private final UUID id;
    private boolean summoned = false;
    
    @SuppressWarnings("unchecked")
    private PetInfoImpl(Path savePath, CompoundTag petTag){
        type = (EntityType<T>)Registry.ENTITY_TYPE.get(new Identifier(petTag.getString("type")));
        id = petTag.getUuid("id");
        this.savePath = savePath.resolve("pets").resolve(id.toString() + ".nbt.gz");
    }
    
    @SuppressWarnings("unchecked")
    private <E extends Entity & PetEntity<T>> PetInfoImpl(Path savePath, E entity){
        type = (EntityType<T>)entity.getType();
        id = entity.petsforever$getId();
        this.savePath = savePath.resolve("pets").resolve(id.toString() + ".nbt.gz");
    
        try{
            Files.createDirectories(this.savePath.getParent());
            try(OutputStream stream = Files.newOutputStream(this.savePath)){
                NbtIo.writeCompressed(entity.petsforever$toTag(), stream);
            }
        }catch(IOException e){
            throw new RuntimeException("Failed to register pet " + entity, e);
        }
    }
    
    public static PetInfo<?> fromTag(Path savePath, CompoundTag petTag){
        return new PetInfoImpl<>(savePath, petTag);
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Entity & PetEntity<T>> PetInfo<T> fromPet(Path savePath, PetEntity<T> pet){
        return new PetInfoImpl(savePath, (Entity & PetEntity<T>)pet);
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
            try(InputStream stream = Files.newInputStream(savePath)){
                entity.petsforever$fromTag(NbtIo.readCompressed(stream));
            }catch(IOException e){
                throw new RuntimeException(e);
            }
            entity.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            world.spawnEntity(entity);
        }
        return entity;
    }
}
