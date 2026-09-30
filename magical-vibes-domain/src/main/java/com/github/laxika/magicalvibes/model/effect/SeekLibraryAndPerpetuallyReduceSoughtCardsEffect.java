package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/** Seeks matching cards into hand and applies a perpetual generic cast-cost reduction to each. */
public record SeekLibraryAndPerpetuallyReduceSoughtCardsEffect(
        DynamicAmount count, CardPredicate filter, DynamicAmount genericCastCostReduction)
        implements CardEffect {

    public SeekLibraryAndPerpetuallyReduceSoughtCardsEffect {
        Objects.requireNonNull(count, "count");
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(genericCastCostReduction, "genericCastCostReduction");
    }
}
