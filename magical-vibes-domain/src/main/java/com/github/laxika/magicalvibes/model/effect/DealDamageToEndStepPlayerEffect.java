package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Deals the supplied amount of damage to the player whose end step is being processed.
 * The end-step player is captured by the trigger machinery rather than chosen as a target.
 */
public record DealDamageToEndStepPlayerEffect(DynamicAmount amount)
        implements EndStepPlayerTargetedEffect, DamageDealingEffect {

    @Override
    public DynamicAmount damageAmount() {
        return amount;
    }

    @Override
    public boolean canDamageCreatures() {
        return false;
    }

    @Override
    public boolean canDamagePlayers() {
        return true;
    }
}
