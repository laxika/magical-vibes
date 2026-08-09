package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Additional cast cost paid once for each mode chosen beyond the first.
 * The chosen permanents are supplied through the multi-permanent cast-cost selection.
 */
public record EscalateSacrificeCost(int count, PermanentPredicate filter) implements CostEffect {

    public EscalateSacrificeCost {
        if (count < 1) {
            throw new IllegalArgumentException("count must be positive");
        }
    }

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return filter;
    }
}
