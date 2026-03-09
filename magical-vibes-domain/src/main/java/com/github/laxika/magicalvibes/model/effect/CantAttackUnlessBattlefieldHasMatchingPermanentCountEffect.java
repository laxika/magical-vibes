package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static attacking restriction: this creature can't attack unless the total number
 * of permanents matching the given predicate across all battlefields meets the minimum count.
 * <p>
 * Example: Harbor Serpent — "can't attack unless there are five or more Islands on the battlefield."
 */
public record CantAttackUnlessBattlefieldHasMatchingPermanentCountEffect(
        PermanentPredicate permanentPredicate,
        int minimumCount,
        String requirementDescription
) implements CardEffect {
}
