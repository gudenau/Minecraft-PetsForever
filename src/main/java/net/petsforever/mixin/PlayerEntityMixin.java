package net.petsforever.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.petsforever.Pets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity{
    @SuppressWarnings("ConstantConditions")
    private PlayerEntityMixin(){
        super(null, null);
    }
    
    @Redirect(
        method = "interact",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;interact(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"
        )
    )
    private ActionResult interact(Entity entity, PlayerEntity player, Hand hand){
        ItemStack stack = getStackInHand(hand);
        if(stack.getItem() == Pets.Items.PERSISTENT_WAND){
            return ActionResult.PASS;
        }else{
            return entity.interact(player, hand);
        }
    }
}
