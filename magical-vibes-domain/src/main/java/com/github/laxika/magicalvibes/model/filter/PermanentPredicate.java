package com.github.laxika.magicalvibes.model.filter;

/**
 * A predicate over a {@link com.github.laxika.magicalvibes.model.Permanent}. Predicates are
 * pure data — evaluation lives in the engine's {@code PredicateEvaluationService}, which
 * switches exhaustively over this sealed hierarchy so that a missing predicate is a compile
 * error rather than a silent runtime fallback.
 */
public sealed interface PermanentPredicate permits
        PermanentAllOfPredicate,
        PermanentAnyOfPredicate,
        PermanentAttachedToSourceControllerPredicate,
        PermanentColorInPredicate,
        PermanentControlledBySourceControllerPredicate,
        PermanentDealtDamageThisTurnPredicate,
        PermanentHasAnySubtypePredicate,
        PermanentHasCountersPredicate,
        PermanentHasGreatestManaValueAmongAllCreaturesPredicate,
        PermanentHasGreatestPowerAmongControlledCreaturesPredicate,
        PermanentHasKeywordPredicate,
        PermanentHasSameNameAsSourcePredicate,
        PermanentHasSubtypePredicate,
        PermanentHasSupertypePredicate,
        PermanentInCombatWithSourcePredicate,
        PermanentIsArtifactPredicate,
        PermanentIsAttackingPredicate,
        PermanentIsAttackingSourceControllerPredicate,
        PermanentIsBlockingPredicate,
        PermanentIsCreaturePredicate,
        PermanentIsEnchantmentPredicate,
        PermanentIsHistoricPredicate,
        PermanentIsLandPredicate,
        PermanentIsPlaneswalkerPredicate,
        PermanentIsSourceCardPredicate,
        PermanentIsTappedPredicate,
        PermanentIsTokenPredicate,
        PermanentManaValueEqualsXPredicate,
        PermanentMaxManaValuePredicate,
        PermanentMinManaValuePredicate,
        PermanentNotPredicate,
        PermanentPowerAtLeastPredicate,
        PermanentPowerAtMostControlledCreatureCountPredicate,
        PermanentPowerAtMostPredicate,
        PermanentPowerAtMostXPredicate,
        PermanentToughnessAtLeastPredicate,
        PermanentToughnessAtMostPredicate,
        PermanentToughnessLessThanSourcePowerPredicate,
        PermanentTruePredicate {
}
