package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;

/** Static permission that removes the normal sorcery-speed restriction from activated abilities. */
public record AllActivatedAbilitiesCanBeActivatedAtInstantSpeedEffect() implements ActivatedAbilityTimingEffect {

    @Override
    public boolean allowsInstantSpeedActivation(ActivatedAbility ability) {
        return ability.getTimingRestriction() == ActivationTimingRestriction.SORCERY_SPEED;
    }
}
