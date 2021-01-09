package net.petsforever.items;

import java.util.List;
import java.util.stream.Collectors;
import net.petsforever.api.PetInfo;
import net.petsforever.api.SharedPetManager;
import net.petsforever.api.UserInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class SummonWandItem extends Item{
    public SummonWandItem(Settings settings){
        super(settings);
    }
    
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
        ItemStack stack = user.getStackInHand(hand);
        
        if(world.isClient){
            return TypedActionResult.success(stack);
        }
    
        UserInfo userInfo = SharedPetManager.getInstance().getUserInfo(user);
        List<PetInfo<?>> pets = userInfo.getPetInfo().stream()
            .filter((info)->info.getType() != null)
            .filter((info)->!info.isSummoned())
            .collect(Collectors.toList());
        
        if(pets.isEmpty()){
            return TypedActionResult.fail(stack);
        }
        
        Entity pet = pets.get(world.getRandom().nextInt(pets.size())).summon(world, user.getBlockPos());
        
        if(pet == null){
            stack.decrement(1);
            return TypedActionResult.success(stack);
        }else{
            return TypedActionResult.fail(stack);
        }
    }
}
