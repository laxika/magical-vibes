package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The player identified by the resolving stack entry's target chooses up to {@code maxCount}
 * matching permanents to keep, then sacrifices the rest of the matching permanents.
 */
public record PlayerChoosesUpToPermanentsThenSacrificesRestEffect(
        int maxCount,
        PermanentPredicate filter) implements CardEffect {

    public PlayerChoosesUpToPermanentsThenSacrificesRestEffect {
        if (maxCount < 0) {
            throw new IllegalArgumentException("maxCount must not be negative");
        }
        if (filter == null) {
            throw new IllegalArgumentException("filter must not be null");
        }
    }
}
