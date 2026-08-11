package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Deals damage to a fixed creature target and boosts the source if the target matches a predicate
 * and actually receives damage.
 */
public record DealDamageToTargetCreatureThenBoostSourceIfDamagedEffect(
        DynamicAmount damage,
        PermanentPredicate boostTargetPredicate,
        DynamicAmount powerBoost,
        DynamicAmount toughnessBoost) implements DamageDealingEffect {

    public DealDamageToTargetCreatureThenBoostSourceIfDamagedEffect(int damage,
                                                                      PermanentPredicate boostTargetPredicate,
                                                                      int powerBoost,
                                                                      int toughnessBoost) {
        this(new Fixed(damage), boostTargetPredicate, new Fixed(powerBoost), new Fixed(toughnessBoost));
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
