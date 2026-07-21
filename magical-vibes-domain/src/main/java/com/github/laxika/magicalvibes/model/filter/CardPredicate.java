package com.github.laxika.magicalvibes.model.filter;

/**
 * A predicate over a {@link com.github.laxika.magicalvibes.model.Card}. Predicates are pure
 * data — evaluation lives in the engine's {@code PredicateEvaluationService}, which switches
 * exhaustively over this sealed hierarchy so that a missing predicate is a compile error
 * rather than a silent runtime fallback.
 */
public sealed interface CardPredicate permits
        CardAllOfPredicate,
        CardAnyOfPredicate,
        CardColorPredicate,
        CardControllerDoesNotOwnPredicate,
        CardHasCyclingPredicate,
        CardHasEmbalmOrEternalizePredicate,
        CardHasFlashbackPredicate,
        CardIsAuraPredicate,
        CardIsHistoricPredicate,
        CardIsMulticoloredPredicate,
        CardIsPermanentPredicate,
        CardIsTokenPredicate,
        CardIsSelfPredicate,
        CardKeywordPredicate,
        CardMaxManaValuePredicate,
        CardMinManaValuePredicate,
        CardNamedPredicate,
        CardNotPredicate,
        CardPowerAtLeastPredicate,
        CardPowerAtMostPredicate,
        CardSharesNameWithAPermanentPredicate,
        CardSubtypePredicate,
        CardSupertypePredicate,
        CardTypePredicate,
        PhyrexianManaPredicate {
}
