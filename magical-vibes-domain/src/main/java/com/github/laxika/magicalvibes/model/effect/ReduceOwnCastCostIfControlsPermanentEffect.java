package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Reduces this spell's casting cost by the given amount if the controller controls at least
 * one permanent matching the predicate.
 */
public record ReduceOwnCastCostIfControlsPermanentEffect(PermanentPredicate predicate, int amount) implements CardEffect {
}
