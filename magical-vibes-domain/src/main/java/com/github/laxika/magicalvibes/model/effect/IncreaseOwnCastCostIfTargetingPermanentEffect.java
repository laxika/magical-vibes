package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Increases this spell's generic casting cost by the given amount if its first target is a
 * permanent matching the predicate.
 */
public record IncreaseOwnCastCostIfTargetingPermanentEffect(PermanentPredicate predicate, int amount)
        implements TargetBasedCastCostIncreaseEffect {
}
