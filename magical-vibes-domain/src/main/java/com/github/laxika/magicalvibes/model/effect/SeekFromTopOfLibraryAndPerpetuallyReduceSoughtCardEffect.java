package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Randomly puts one matching top-library card into hand and perpetually reduces its generic cast cost. */
public record SeekFromTopOfLibraryAndPerpetuallyReduceSoughtCardEffect(
        int count, CardPredicate filter, int costReduction) implements CardEffect {

    public SeekFromTopOfLibraryAndPerpetuallyReduceSoughtCardEffect(int count, CardPredicate filter) {
        this(count, filter, 1);
    }

    public SeekFromTopOfLibraryAndPerpetuallyReduceSoughtCardEffect {
        if (count < 1) {
            throw new IllegalArgumentException("count must be positive");
        }
        if (costReduction < 1) {
            throw new IllegalArgumentException("costReduction must be positive");
        }
    }
}
