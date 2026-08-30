package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Describes a spell-self generic cast-cost increase applied when the spell's first target is a
 * permanent matching the predicate.
 */
public interface TargetBasedCastCostIncreaseEffect extends CardEffect {

    PermanentPredicate predicate();

    int amount();
}
