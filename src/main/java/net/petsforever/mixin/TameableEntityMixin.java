package net.petsforever.mixin;

import java.util.UUID;
import net.gudenau.minecraft.pets.api.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.world.GameRules;
import net.petsforever.api.PetEntity;
import net.petsforever.api.SharedPetManager;
import net.petsforever.api.UserInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TameableEntity.class)
public abstract class TameableEntityMixin<E extends Entity> extends AnimalEntity implements PetEntity<E>{
    @Shadow @Nullable public abstract UUID getOwnerUuid();
    
    @Shadow @Nullable public abstract LivingEntity getOwner();
    
    @SuppressWarnings("ConstantConditions")
    private TameableEntityMixin(){
        super(null, null);
    }
    
    @Override
    public UUID gud_pets$getId(){
        return getUuid();
    }
    
    @Override
    public UUID gud_pets$getOwnerId(){
        return getOwnerUuid();
    }
    
    @Inject(
        method = "onDeath",
        at = @At("HEAD"),
        cancellable = true
    )
    public void onDeath(DamageSource source, CallbackInfo ci){
        UserInfo info = SharedPetManager.getInstance().getUserInfo(getOwnerUuid()).orElse(null);
        if(info != null){
            if(info.ownsPet(this)){
                info.getPetInfo(getUuid()).ifPresent((petInfo)->petInfo.setSummoned(false));
                LivingEntity owner = getOwner();
                if(!world.isClient && world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES) && owner instanceof ServerPlayerEntity){
                    owner.sendSystemMessage(new TranslatableText("asdf", getDisplayName()), Util.NIL_UUID);
                }
                super.onDeath(source);
                ci.cancel();
            }
        }
    }
}
