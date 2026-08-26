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
        CardHasDisturbPredicate,
        CardHasCyclingPredicate,
        CardHasAdventurePredicate,
        CardHasEmbalmOrEternalizePredicate,
        CardHasForetellPredicate,
        CardHasFlashbackPredicate,
        CardHasManaAbilityPredicate,
        CardHasSourceChosenColorPredicate,
        CardHasSourceChosenCardTypePredicate,
        CardHasExactlyTwoColorsPredicate,
        CardHasNoAbilitiesPredicate,
        CardHasSourceChosenSubtypePredicate,
        CardIsAuraEnchantCreaturePredicate,
        CardIsAuraPredicate,
        CardIsColorlessPredicate,
        CardIsDoubleFacedPredicate,
        CardIsHistoricPredicate,
        CardIsMulticoloredPredicate,
        CardIsPermanentPredicate,
        CardIsTokenPredicate,
        CardIsSelfPredicate,
        CardKeywordPredicate,
        CardNameInControllerGraveyardPredicate,
        CardManaValueAtMostPermanentCardsInControllerGraveyardPredicate,
        CardManaValueAtMostSourcePowerPredicate,
        CardManaValueLessThanSourceLoyaltyPredicate,
        CardMaxManaValuePredicate,
        CardMaxManaValueXPredicate,
        CardMinManaValuePredicate,
        CardNamedPredicate,
        CardNotPredicate,
        CardPowerAtLeastPredicate,
        CardPowerAtMostPredicate,
        CardPowerToughnessTotalAtMostPredicate,
        CardToughnessAtLeastPredicate,
        CardToughnessGreaterThanPowerPredicate,
        CardSharesCardTypeWithImprintedCardPredicate,
        CardToughnessLessThanSourceToughnessPredicate,
        CardSharesNameWithAPermanentPredicate,
        CardSubtypePredicate,
        CardSupertypePredicate,
        CardTruePredicate,
        CardTypePredicate,
        PhyrexianManaPredicate {
}
