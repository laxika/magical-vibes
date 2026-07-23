package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Evaluates to {@code amount} when the resolved target permanent matches {@code filter} at
 * evaluation time, and to {@code otherwise} when the target is missing, is a player, or fails
 * the predicate.
 *
 * <p>Models "do N … If it's a [matching permanent], do M instead" wordings checked on resolution
 * (Elvish Healer: prevent 1, or 2 if the target is a green creature).
 */
public record FixedIfTargetMatches(PermanentPredicate filter, int amount, int otherwise)
        implements DynamicAmount {
}
