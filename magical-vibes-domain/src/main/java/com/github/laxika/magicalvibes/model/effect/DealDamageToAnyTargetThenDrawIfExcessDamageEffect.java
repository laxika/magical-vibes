package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Deals damage to any target, then draws a card if excess damage hit a creature. */
public record DealDamageToAnyTargetThenDrawIfExcessDamageEffect(DynamicAmount damage)
        implements DamageDealingEffect, CardDrawingEffect {

    public DealDamageToAnyTargetThenDrawIfExcessDamageEffect(int damage) {
        this(new Fixed(damage));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.anyTarget());
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
        return true;
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(1);
    }
}
