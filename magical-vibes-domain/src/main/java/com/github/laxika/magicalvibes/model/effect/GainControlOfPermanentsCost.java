package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Gain control of the requested number of permanents matching {@code filter} that the payer does
 * not currently control.
 */
public record GainControlOfPermanentsCost(int count, PermanentPredicate filter) implements CostEffect {

    public GainControlOfPermanentsCost {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive");
        }
        if (filter == null) {
            throw new IllegalArgumentException("filter must not be null");
        }
    }
}
