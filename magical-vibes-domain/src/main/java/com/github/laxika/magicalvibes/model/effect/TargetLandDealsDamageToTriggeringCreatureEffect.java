package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** The selected land deals fixed damage to the permanent that caused the delayed trigger. */
public record TargetLandDealsDamageToTriggeringCreatureEffect(int damage)
        implements DamageDealingEffect {

    public TargetLandDealsDamageToTriggeringCreatureEffect() {
        this(3);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.land());
    }

    @Override
    public DynamicAmount damageAmount() {
        return new Fixed(damage);
    }

    @Override
    public boolean canDamageCreatures() {
        return true;
    }

    @Override
    public boolean canDamagePlayers() {
        return false;
    }
}
