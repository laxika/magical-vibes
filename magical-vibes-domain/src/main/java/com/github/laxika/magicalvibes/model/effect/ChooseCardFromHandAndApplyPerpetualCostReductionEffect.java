package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Chooses a matching card in hand and permanently reduces its generic cast cost. */
public record ChooseCardFromHandAndApplyPerpetualCostReductionEffect(
        CardPredicate cardFilter, int amount) implements CardEffect {

    public ChooseCardFromHandAndApplyPerpetualCostReductionEffect {
        if (amount < 1) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }
}
