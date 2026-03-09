package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/**
 * Gains life equal to the sum of permanents matching each predicate on the battlefield.
 * Each predicate is counted separately, so a permanent matching multiple predicates
 * is counted once per matching predicate (e.g. an artifact creature counts for both
 * {@code PermanentIsCreaturePredicate} and {@code PermanentIsArtifactPredicate}).
 */
public record GainLifePerMatchingPermanentOnBattlefieldEffect(
        List<PermanentPredicate> predicates
) implements CardEffect {
}
