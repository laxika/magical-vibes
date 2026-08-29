package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Deals damage to the end-step player unless one of that player's opponents was dealt damage this
 * turn. The condition is checked when the effect resolves, so damage dealt after the trigger was
 * put on the stack can still prevent this damage.
 */
public record DealDamageToEndStepPlayerUnlessOpponentWasDealtDamageEffect(int damage)
        implements EndStepPlayerTargetedEffect, DamageDealingEffect {

    @Override
    public DynamicAmount damageAmount() {
        return new Fixed(damage);
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
