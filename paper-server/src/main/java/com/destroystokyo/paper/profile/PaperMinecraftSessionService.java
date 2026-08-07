package com.destroystokyo.paper.profile;

import com.destroystokyo.paper.event.profile.FillProfileEvent;
import com.destroystokyo.paper.event.profile.PreFillProfileEvent;
import com.mojang.authlib.services.MinecraftServicesDiscoveryService;
import com.mojang.authlib.services.MinecraftServicesSessionService;
import com.mojang.authlib.services.ProfileResult;
import com.mojang.authlib.services.ServicesKeySet;

import java.net.Proxy;
import java.util.Collections;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public class PaperMinecraftSessionService extends MinecraftServicesSessionService {

    public PaperMinecraftSessionService(ServicesKeySet servicesKeySet, Proxy proxy, MinecraftServicesDiscoveryService discoveryService) {
        super(servicesKeySet, proxy, discoveryService);
    }

    @Override
    public @Nullable ProfileResult fetchProfile(final UUID profileId, final boolean requireSecure) {
        CraftPlayerProfile playerProfile = new CraftPlayerProfile(profileId, null);
        new PreFillProfileEvent(playerProfile).callEvent();
        if (playerProfile.getGameProfile().properties().containsKey("textures")) {
            return new ProfileResult(playerProfile.getGameProfile(), Collections.emptySet());
        }
        ProfileResult result = super.fetchProfile(profileId, requireSecure);
        if (result != null) {
            final FillProfileEvent event = new FillProfileEvent(CraftPlayerProfile.asBukkitCopy(result.profile()));
            event.callEvent();
            result = new ProfileResult(CraftPlayerProfile.asAuthlibCopy(event.getPlayerProfile()), result.actions());
        }
        return result;
    }
}
