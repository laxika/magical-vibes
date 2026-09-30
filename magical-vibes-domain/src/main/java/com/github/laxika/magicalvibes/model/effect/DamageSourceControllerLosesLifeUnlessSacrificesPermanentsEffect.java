package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Damage trigger: the damage source's controller sacrifices that many permanents or loses that
 * much life.
 */
public record DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffect(
        int amount, UUID sacrificingPlayerId) implements DamageSourceControllerAwareEffect {

    @Override
    public CardEffect bindDamageSourceController(UUID controllerId, int damageDealt) {
        if (controllerId == null || damageDealt <= 0) return this;
        return new DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffect(
                damageDealt, controllerId);
    }
}
