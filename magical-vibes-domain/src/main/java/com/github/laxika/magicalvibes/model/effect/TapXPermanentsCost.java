package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Cost effect that taps X untapped permanents matching the given filter,
 * where X is the xValue chosen by the player at activation time.
 * Unlike {@link TapMultiplePermanentsCost}, the count is dynamic.
 *
 * <p>Example: "Tap X untapped Knights you control" (Aryel, Knight of Windgrace).
 */
public record TapXPermanentsCost(PermanentPredicate filter, boolean excludeSource) implements CostEffect {

    public TapXPermanentsCost(PermanentPredicate filter) {
        this(filter, false);
    }
}
