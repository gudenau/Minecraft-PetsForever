package net.petsforever.items;

import java.util.UUID;
import net.petsforever.api.PetEntity;
import net.petsforever.api.SharedPetManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class PersistentWandItem extends Item{
    public PersistentWandItem(Settings settings){
        super(settings);
    }
    
    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand){
        if(user.world.isClient){
            return ActionResult.SUCCESS;
        }
        
        if(!(entity instanceof PetEntity)){
            user.sendMessage(new TranslatableText("message.petsforever.not_a_pet"), true);
            return ActionResult.FAIL;
        }
        
        UUID ownerId = ((PetEntity<?>)entity).petsforever$getOwnerId();
        if(!ownerId.equals(user.getUuid())){
            user.sendMessage(new TranslatableText("message.petsforever.not_yours"), true);
            return ActionResult.FAIL;
        }
        
        boolean result = SharedPetManager.getInstance()
            .getUserInfo(user)
            .registerPet(entity);
        
        if(result){
            user.sendMessage(new TranslatableText("message.petsforever.registered"), true);
            stack.decrement(1);
            user.sendToolBreakStatus(hand);
            return ActionResult.SUCCESS;
        }else{
            user.sendMessage(new TranslatableText("message.petsforever.too_many_pets"), true);
            return ActionResult.FAIL;
        }
    }
}
