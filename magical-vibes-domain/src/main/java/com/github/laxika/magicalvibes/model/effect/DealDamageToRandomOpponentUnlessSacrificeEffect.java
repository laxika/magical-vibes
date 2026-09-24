package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Chooses one opponent at random. That player may sacrifice a matching permanent; if they do not,
 * the source deals the configured amount of damage to them.
 */
public record DealDamageToRandomOpponentUnlessSacrificeEffect(
        DynamicAmount damage, PermanentPredicate sacrificeFilter) implements CardEffect, DamageDealingEffect {

    public DealDamageToRandomOpponentUnlessSacrificeEffect {
        if (damage == null || sacrificeFilter == null) {
            throw new IllegalArgumentException("Damage amount and sacrifice filter are required");
        }
    }

    public DealDamageToRandomOpponentUnlessSacrificeEffect(PermanentPredicate sacrificeFilter) {
        this(new SourcePower(), sacrificeFilter);
    }

    @Override
    public DynamicAmount damageAmount() {
        return damage;
    }

    @Override
    public boolean canDamageCreatures() {
        return false;
    }

    @Override
    public boolean canDamagePlayers() {
        return true;
    }
}
