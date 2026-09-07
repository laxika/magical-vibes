package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

/**
 * Deals damage to any target other than the source permanent. Each player actually dealt damage
 * by this effect can't gain life for the rest of the game.
 */
public record DealDamageToAnyOtherTargetThenLockPlayerLifeGainEffect(DynamicAmount damage)
        implements DamageDealingEffect {

    public DealDamageToAnyOtherTargetThenLockPlayerLifeGainEffect(int damage) {
        this(new Fixed(damage));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.anyTarget(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()));
    }

    @Override
    public TargetFilter triggeredTargetFilter() {
        return DealDamageToAnyTargetEffect.anyOtherTargetFilter();
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
