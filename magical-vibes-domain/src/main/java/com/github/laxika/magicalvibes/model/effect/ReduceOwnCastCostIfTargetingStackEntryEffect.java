package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

/**
 * Reduces this spell's casting cost by the given amount if its first target is a spell on the stack
 * matching the predicate.
 */
public record ReduceOwnCastCostIfTargetingStackEntryEffect(StackEntryPredicate predicate, int amount) implements CardEffect {
}
