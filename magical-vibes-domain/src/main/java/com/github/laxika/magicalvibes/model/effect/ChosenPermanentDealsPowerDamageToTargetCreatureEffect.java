package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.ChosenPermanentPower;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/** The permanent recorded as chosen deals damage equal to its power to target creature. */
public record ChosenPermanentDealsPowerDamageToTargetCreatureEffect() implements DamageDealingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public DynamicAmount damageAmount() {
        return new ChosenPermanentPower();
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
