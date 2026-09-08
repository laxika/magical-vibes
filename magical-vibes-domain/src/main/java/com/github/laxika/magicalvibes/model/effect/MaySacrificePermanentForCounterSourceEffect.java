package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "You may sacrifice a permanent matching {@code filter}. If you do, put a +1/+1 counter on this
 * creature." A decline has no effect.
 */
public record MaySacrificePermanentForCounterSourceEffect(
        PermanentPredicate filter,
        String description
) implements CardEffect {
}
