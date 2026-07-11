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
        StackEntryColorInPredicate,
        StackEntryControlledByEnchantedPlayerPredicate,
        StackEntryControlledByPredicate,
        StackEntryHasTargetPredicate,
        StackEntryIsSingleTargetPredicate,
        StackEntryManaValuePredicate,
        StackEntryManaValueAtMostControlledCountPredicate,
        StackEntryNotPredicate,
        StackEntrySharesChosenNameWithSourcePredicate,
        StackEntryTargetsPermanentPredicate,
        StackEntryTargetsYouOrCreatureYouControlPredicate,
        StackEntryTargetsYourPermanentPredicate,
        StackEntrySubtypeInPredicate,
        StackEntryTypeInPredicate {
}
