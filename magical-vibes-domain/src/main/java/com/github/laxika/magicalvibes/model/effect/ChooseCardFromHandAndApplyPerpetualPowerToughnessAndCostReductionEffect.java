package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Chooses one hand card and perpetually changes both its P/T and generic cast cost. */
public record ChooseCardFromHandAndApplyPerpetualPowerToughnessAndCostReductionEffect(
        CardPredicate cardFilter, int power, int toughness, int costReduction) implements CardEffect {

    public ChooseCardFromHandAndApplyPerpetualPowerToughnessAndCostReductionEffect {
        if (costReduction < 1) {
            throw new IllegalArgumentException("costReduction must be positive");
        }
    }
}
