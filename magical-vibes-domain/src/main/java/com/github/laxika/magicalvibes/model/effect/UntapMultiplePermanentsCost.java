package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Untap N tapped permanents matching {@code filter} as a cost. With {@code opponentControlled} the
 * choices come from opponents' battlefields instead of the activating player's ("Untap a tapped land
 * an opponent controls", Benthic Explorers); a single-permanent instance also records the untapped
 * permanent on the source so {@link AwardManaOfTypeUntappedLandCouldProduceEffect} can read it.
 */
public record UntapMultiplePermanentsCost(int count, PermanentPredicate filter, boolean excludeSource,
                                          boolean opponentControlled) implements CostEffect {

    public UntapMultiplePermanentsCost(int count, PermanentPredicate filter) {
        this(count, filter, false, false);
    }

    public UntapMultiplePermanentsCost(int count, PermanentPredicate filter, boolean excludeSource) {
        this(count, filter, excludeSource, false);
    }
}
