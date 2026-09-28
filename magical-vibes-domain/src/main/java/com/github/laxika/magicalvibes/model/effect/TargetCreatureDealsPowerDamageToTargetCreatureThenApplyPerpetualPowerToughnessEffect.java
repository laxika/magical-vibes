package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Deals power damage from the first target to the second, then boosts a chosen creature card by the excess. */
public record TargetCreatureDealsPowerDamageToTargetCreatureThenApplyPerpetualPowerToughnessEffect(
        CardPredicate handCardFilter,
        int sourceTargetGroup,
        int victimTargetGroup) implements CardEffect {

    public TargetCreatureDealsPowerDamageToTargetCreatureThenApplyPerpetualPowerToughnessEffect(
            CardPredicate handCardFilter) {
        this(handCardFilter, 0, 1);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
