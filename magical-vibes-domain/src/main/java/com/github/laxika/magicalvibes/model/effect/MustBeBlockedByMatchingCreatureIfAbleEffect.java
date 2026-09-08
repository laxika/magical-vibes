package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static requirement that at least one creature matching {@code blockerFilter} block this
 * permanent whenever such a block is legally possible.
 */
public record MustBeBlockedByMatchingCreatureIfAbleEffect(PermanentPredicate blockerFilter)
        implements CardEffect {
}
