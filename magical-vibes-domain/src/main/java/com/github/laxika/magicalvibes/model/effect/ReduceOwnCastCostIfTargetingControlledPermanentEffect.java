package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Reduces this spell's casting cost by the given amount if its first target is a permanent
 * controlled by the caster and matching the predicate.
 */
public record ReduceOwnCastCostIfTargetingControlledPermanentEffect(PermanentPredicate predicate, int amount) implements CardEffect {
}
