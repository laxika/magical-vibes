package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Each player gains permanent control of each matching permanent they own. */
public record EachPlayerGainsControlOfOwnedPermanentsMatchingEffect(PermanentPredicate filter)
        implements CardEffect {

    public EachPlayerGainsControlOfOwnedPermanentsMatchingEffect {
        if (filter == null) {
            throw new IllegalArgumentException("filter must not be null");
        }
    }
}
