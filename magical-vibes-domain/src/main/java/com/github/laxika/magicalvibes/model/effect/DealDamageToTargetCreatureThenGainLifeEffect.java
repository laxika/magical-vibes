package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Deals damage to a target creature, then the controller gains life equal to the damage actually
 * dealt by this effect.
 */
public record DealDamageToTargetCreatureThenGainLifeEffect(DynamicAmount damage)
        implements DamageDealingEffect, LifeGainEffect {

    public DealDamageToTargetCreatureThenGainLifeEffect(int damage) {
        this(new Fixed(damage));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public DynamicAmount damageAmount() {
        return damage;
    }

    @Override
    public boolean canDamageCreatures() {
        return true;
    }

    @Override
    public boolean canDamagePlayers() {
        return false;
    }

    @Override
    public DynamicAmount lifeGainAmount() {
        return damage;
    }
}
