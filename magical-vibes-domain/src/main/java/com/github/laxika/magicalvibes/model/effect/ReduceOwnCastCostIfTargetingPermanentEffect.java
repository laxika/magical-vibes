package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Reduces this spell's casting cost by the given amount if its first target is a permanent
 * matching the predicate.
 */
public record ReduceOwnCastCostIfTargetingPermanentEffect(PermanentPredicate predicate, int amount) implements CardEffect {
}
