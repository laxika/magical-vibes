package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Additional cast cost: discard a card or sacrifice one matching permanent.
 * Exactly one of the two options is paid.
 */
public record DiscardCardOrSacrificePermanentCost(PermanentPredicate filter, String description)
        implements CostEffect {

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return filter;
    }
}
