package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Deals damage to target creature and each other creature on the battlefield with the same name.
 * Only the named target is targeted — same-name creatures (including hexproof ones) are still hit.
 * Fizzles if the target leaves. Used by Izzet Staticaster.
 */
public record DealDamageToTargetCreatureAndAllWithSameNameEffect(DynamicAmount damage)
        implements DamageDealingEffect {

    public DealDamageToTargetCreatureAndAllWithSameNameEffect(int damage) {
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
