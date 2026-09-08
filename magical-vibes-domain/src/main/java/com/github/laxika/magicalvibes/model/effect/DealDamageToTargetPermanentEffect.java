package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Deals damage to target permanent that can receive damage. */
public record DealDamageToTargetPermanentEffect(DynamicAmount damage)
        implements DamageDealingEffect, CombatDamageAmountAwareEffect {

    public DealDamageToTargetPermanentEffect(int damage) {
        this(new Fixed(damage));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }

    @Override
    public DynamicAmount damageAmount() {
        return damage;
    }

    @Override
    public DynamicAmount combatDamageAmount() {
        return damage;
    }

    @Override
    public CardEffect snapshotCombatDamage(int damageDealt) {
        return damage instanceof EventValue
                ? new DealDamageToTargetPermanentEffect(damageDealt)
                : this;
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
