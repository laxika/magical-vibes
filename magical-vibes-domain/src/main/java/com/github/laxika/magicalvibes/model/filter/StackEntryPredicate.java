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
        StackEntryControlledByEnchantedPlayerPredicate,
        StackEntryControlledByPredicate,
        StackEntryHasTargetPredicate,
        StackEntryHasXInManaCostPredicate,
        StackEntryIsNthSpellCastThisTurnPredicate,
        StackEntryKickedPredicate,
        StackEntryIsSingleTargetPredicate,
        StackEntryManaValuePredicate,
        StackEntryMaxManaValuePredicate,
        StackEntryManaValueEqualsXPredicate,
        StackEntryManaValueEqualsSourceCountersPredicate,
        StackEntryManaValueEqualsSourcePowerPredicate,
        StackEntryManaValuePowerOrToughnessEqualsSourceChosenNumberPredicate,
        StackEntryManaValueAtMostControlledCountPredicate,
        StackEntryManaValueAtMostControllerGraveyardCountPredicate,
        StackEntrySharesColorOrManaValueWithImprintedCardPredicate,
        StackEntryNotPredicate,
        StackEntryNotTargetedByNamedCreatureAbilityPredicate,
        StackEntrySharesChosenNameWithSourcePredicate,
        StackEntrySharesNameWithCardExiledWithSourcePredicate,
        StackEntrySupertypeInPredicate,
        StackEntryTargetsAnyPlayerPredicate,
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
