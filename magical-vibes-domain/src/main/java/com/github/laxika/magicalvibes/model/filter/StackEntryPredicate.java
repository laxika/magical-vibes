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
        StackEntryControlledByEnchantedPlayerPredicate,
        StackEntryControlledByPredicate,
        StackEntryHasTargetPredicate,
        StackEntryHasXInManaCostPredicate,
        StackEntryIsNthSpellCastThisTurnPredicate,
        StackEntryIsSingleTargetPredicate,
        StackEntryManaValuePredicate,
        StackEntryMaxManaValuePredicate,
        StackEntryManaValueEqualsXPredicate,
        StackEntryManaValueEqualsSourceCountersPredicate,
        StackEntryManaValueAtMostControlledCountPredicate,
        StackEntrySharesColorOrManaValueWithImprintedCardPredicate,
        StackEntryNotPredicate,
        StackEntrySharesChosenNameWithSourcePredicate,
        StackEntrySupertypeInPredicate,
        StackEntryTargetsAnyPlayerPredicate,
        StackEntryTargetsPermanentPredicate,
        StackEntryTargetsSourcePredicate,
        StackEntryTargetsYouOrCreatureYouControlPredicate,
        StackEntryTargetsYouPredicate,
        StackEntryTargetsYourPermanentPredicate,
        StackEntrySubtypeInPredicate,
        StackEntryTruePredicate,
        StackEntryTypeInPredicate {
}
