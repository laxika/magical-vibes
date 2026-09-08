package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Additional cast cost that taps one untapped matching permanent for each mode chosen beyond the
 * first. The chosen permanents are supplied through the multi-permanent cast-cost selection.
 */
public record EscalateTapCost(PermanentPredicate filter) implements CostEffect {

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return filter;
    }
}
