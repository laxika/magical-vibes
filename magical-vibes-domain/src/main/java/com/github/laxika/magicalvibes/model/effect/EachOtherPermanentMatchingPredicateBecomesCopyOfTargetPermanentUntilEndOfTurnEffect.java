package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Each other permanent matching {@code affectedPredicate} becomes a copy of the target permanent
 * until end of turn. The target and affected-permanent predicates are evaluated when the effect
 * resolves.
 */
public record EachOtherPermanentMatchingPredicateBecomesCopyOfTargetPermanentUntilEndOfTurnEffect(
        PermanentPredicate targetPredicate,
        PermanentPredicate affectedPredicate,
        boolean removeLegendary,
        Duration duration) implements CardEffect {

    public EachOtherPermanentMatchingPredicateBecomesCopyOfTargetPermanentUntilEndOfTurnEffect(
            PermanentPredicate targetPredicate, PermanentPredicate affectedPredicate) {
        this(targetPredicate, affectedPredicate, false, Duration.UNTIL_END_OF_TURN);
    }

    public EachOtherPermanentMatchingPredicateBecomesCopyOfTargetPermanentUntilEndOfTurnEffect(
            PermanentPredicate targetPredicate, PermanentPredicate affectedPredicate,
            boolean removeLegendary) {
        this(targetPredicate, affectedPredicate, removeLegendary, Duration.UNTIL_END_OF_TURN);
    }

    public static EachOtherPermanentMatchingPredicateBecomesCopyOfTargetPermanentUntilEndOfTurnEffect
    untilNextEndStep(PermanentPredicate targetPredicate, PermanentPredicate affectedPredicate) {
        return new EachOtherPermanentMatchingPredicateBecomesCopyOfTargetPermanentUntilEndOfTurnEffect(
                targetPredicate, affectedPredicate, false, Duration.UNTIL_NEXT_END_STEP);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(), targetPredicate);
    }
}
