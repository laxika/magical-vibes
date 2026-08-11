package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;

/** Static permission that removes the normal sorcery-speed restriction from equip abilities. */
public record EquipAbilitiesCanBeActivatedAtInstantSpeedEffect() implements ActivatedAbilityTimingEffect {

    @Override
    public boolean allowsInstantSpeedActivation(ActivatedAbility ability) {
        return ability.getTimingRestriction() == ActivationTimingRestriction.SORCERY_SPEED
                && ability.getEffects().stream().anyMatch(EquipEffect.class::isInstance);
    }
}
