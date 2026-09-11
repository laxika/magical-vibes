package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Deals damage to any target and destroys it if it matches a predicate and actually receives
 * damage.
 */
public record DealDamageToAnyTargetThenDestroyIfDamagedEffect(
        DynamicAmount damage,
        PermanentPredicate destroyTargetPredicate) implements DamageDealingEffect {

    public DealDamageToAnyTargetThenDestroyIfDamagedEffect(int damage,
                                                            PermanentPredicate destroyTargetPredicate) {
        this(new Fixed(damage), destroyTargetPredicate);
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

}
