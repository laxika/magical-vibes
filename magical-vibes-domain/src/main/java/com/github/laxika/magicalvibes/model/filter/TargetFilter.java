package com.github.laxika.magicalvibes.model.filter;

/**
 * A filter restricting what a spell or ability may target. Filters are pure data —
 * evaluation lives in the engine's {@code PredicateEvaluationService} (and the targeting
 * services for stack/player filters), which dispatch over this sealed hierarchy.
 */
public sealed interface TargetFilter permits
        AnyTargetPredicateTargetFilter,
        ControlledPermanentPredicateTargetFilter,
        OwnedPermanentPredicateTargetFilter,
        PermanentPredicateTargetFilter,
        PlayerPredicateTargetFilter,
        StackEntryPredicateTargetFilter {
}
