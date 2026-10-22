package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The source permanent deals damage equal to its power to target creature.
 *
 * @param targetFilter optional narrowing of the creature that may be targeted (for example "creature an
 *                     opponent controls"), or {@code null} for any creature
 */
public record SourcePermanentDealsPowerDamageToTargetCreatureEffect(PermanentPredicate targetFilter)
        implements DamageDealingEffect {

    public SourcePermanentDealsPowerDamageToTargetCreatureEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetFilter == null
                ? TargetSpec.harmful(TargetPredicates.creature())
                : TargetSpec.harmful(TargetPredicates.creature(), targetFilter);
    }

    @Override
    public DynamicAmount damageAmount() {
        return new SourcePower();
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
