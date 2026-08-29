package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Describes a spell-self cost reduction counted once for every chosen target permanent matching
 * the predicate. Target occurrences are counted separately, so the same permanent can contribute
 * more than once when a spell targets it more than once.
 */
public interface PerTargetCastCostReductionEffect extends CardEffect {

    PermanentPredicate predicate();

    int amount();
}
