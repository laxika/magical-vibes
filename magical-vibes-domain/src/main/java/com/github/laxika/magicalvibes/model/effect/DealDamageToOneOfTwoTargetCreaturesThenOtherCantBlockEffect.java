package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * The controller of two targeted creatures chooses one to receive damage; the other can't block
 * this turn.
 */
public record DealDamageToOneOfTwoTargetCreaturesThenOtherCantBlockEffect(DynamicAmount damage)
        implements DamageDealingEffect {

    public DealDamageToOneOfTwoTargetCreaturesThenOtherCantBlockEffect(int damage) {
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
}
