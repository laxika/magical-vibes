package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Each player loses {@code lifePerPermanent} life for each matching permanent they control.
 * The matching predicate is evaluated separately against each player's battlefield.
 */
public record EachPlayerLosesLifePerPermanentControlledEffect(int lifePerPermanent,
                                                               PermanentPredicate filter)
        implements CardEffect {
}
