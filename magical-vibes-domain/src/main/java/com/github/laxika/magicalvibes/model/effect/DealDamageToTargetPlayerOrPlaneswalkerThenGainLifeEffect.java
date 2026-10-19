package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Deals damage to target player or planeswalker, then the controller gains life equal to the damage
 * actually dealt by this effect (Rite of Consumption). Prevention applies before the life is gained.
 *
 * @param damage the amount of damage to deal, evaluated at resolution
 */
public record DealDamageToTargetPlayerOrPlaneswalkerThenGainLifeEffect(DynamicAmount damage)
        implements DamageDealingEffect, LifeGainEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.playerOrPlaneswalker());
    }

    @Override
    public DynamicAmount damageAmount() {
        return damage;
    }

    @Override
    public boolean canDamageCreatures() {
        return false;
    }

    @Override
    public boolean canDamagePlayers() {
        return true;
    }

    @Override
    public DynamicAmount lifeGainAmount() {
        return damage;
    }
}
