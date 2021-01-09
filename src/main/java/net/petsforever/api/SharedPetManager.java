package net.petsforever.api;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.petsforever.impl.SharedPetManagerImpl;
import net.minecraft.entity.player.PlayerEntity;

public interface SharedPetManager{
    static SharedPetManager getInstance(){
        return SharedPetManagerImpl.INSTANCE;
    }
    
    Set<UserInfo> getUsers();
    Optional<UserInfo> getUserInfo(UUID uuid);
    UserInfo getUserInfo(PlayerEntity player);
}
