package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Additional cast cost: sacrifice a matching permanent or discard a card.
 * Exactly one option is paid through the cast request's permanent or hand-card selection.
 */
public record SacrificePermanentOrDiscardCardCost(PermanentPredicate filter, String description)
        implements CostEffect {

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return filter;
    }
}
