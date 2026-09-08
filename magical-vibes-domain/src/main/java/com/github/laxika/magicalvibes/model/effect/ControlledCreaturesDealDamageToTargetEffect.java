package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Each matching creature controlled by the effect's controller deals a fixed amount of damage to
 * the target creature. Each matching creature is a separate damage source.
 */
public record ControlledCreaturesDealDamageToTargetEffect(
        DynamicAmount damage,
        PermanentPredicate filter
) implements DamageDealingEffect {

    public ControlledCreaturesDealDamageToTargetEffect(int damage, PermanentPredicate filter) {
        this(new Fixed(damage), filter);
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
