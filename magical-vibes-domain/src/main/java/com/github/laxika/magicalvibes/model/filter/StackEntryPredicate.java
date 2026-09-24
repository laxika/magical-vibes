package com.github.laxika.magicalvibes.model.filter;

/**
 * A predicate over a {@link com.github.laxika.magicalvibes.model.StackEntry}. Predicates are
 * pure data — evaluation lives in the engine (the {@code PredicateEvaluationService} for
 * static-effect contexts and {@code TargetLegalityService} for targeting contexts), each of
 * which dispatches over this sealed hierarchy.
 */
public sealed interface StackEntryPredicate permits
        StackEntryAllOfPredicate,
        StackEntryAnyOfPredicate,
        StackEntryCardTypeInPredicate,
        StackEntryCastFromZonePredicate,
        StackEntryColorInPredicate,
        StackEntryIsMulticoloredPredicate,
        StackEntryControlledByChosenPlayerPredicate,
        StackEntryControlledByEnchantedPlayerPredicate,
        StackEntryControlledByPredicate,
        StackEntryHasTargetPredicate,
        StackEntryHasXInManaCostPredicate,
        StackEntryIsNthSpellCastThisTurnPredicate,
        StackEntryKickedPredicate,
        StackEntryIsSingleTargetPredicate,
        StackEntryManaValuePredicate,
        StackEntryMaxManaValuePredicate,
        StackEntryManaSpentLessThanManaValuePredicate,
        StackEntryManaValueEqualsXPredicate,
        StackEntryManaValueEqualsSourceCountersPredicate,
        StackEntryManaValueGreaterThanControllerExperienceCountersPredicate,
        StackEntryManaValueEqualsSourcePowerPredicate,
        StackEntryManaValueAtMostSourcePowerPredicate,
        StackEntryManaValuePowerOrToughnessEqualsSourceChosenNumberPredicate,
        StackEntryManaValueParityMatchesSourceChosenParityPredicate,
        StackEntryManaValueAtMostControlledCountPredicate,
        StackEntryManaValueAtMostControllerGraveyardCountPredicate,
        StackEntrySharesColorOrManaValueWithImprintedCardPredicate,
        StackEntryNotPredicate,
        StackEntryIsCopyPredicate,
        StackEntryNotTargetedByNamedCreatureAbilityPredicate,
        StackEntrySharesChosenNameWithSourcePredicate,
        StackEntrySharesNameWithCardExiledWithSourcePredicate,
        StackEntrySupertypeInPredicate,
        StackEntryTargetsAnyPlayerPredicate,
        StackEntryTargetsOnlySinglePermanentOrPlayerPredicate,
        StackEntryTargetsOnlySingleCreaturePredicate,
        StackEntryTargetsPermanentPredicate,
        StackEntryTargetsSourcePredicate,
        StackEntryTargetsYouOrCreatureYouControlPredicate,
        StackEntryTargetsYouPredicate,
        StackEntryTargetsYourPermanentPredicate,
        StackEntrySubtypeInPredicate,
        StackEntryTruePredicate,
        StackEntryTypeInPredicate {
}
