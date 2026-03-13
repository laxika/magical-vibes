package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/**
 * Gains life equal to the number of permanents the controller has that match the given predicates.
 * Each predicate is counted separately, so a permanent matching multiple predicates
 * is counted once per matching predicate.
 */
public record GainLifePerControlledMatchingPermanentEffect(
        List<PermanentPredicate> predicates
) implements CardEffect {
}
