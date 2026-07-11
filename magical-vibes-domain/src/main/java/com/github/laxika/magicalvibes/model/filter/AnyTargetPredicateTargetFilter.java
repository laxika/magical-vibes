package com.github.laxika.magicalvibes.model.filter;

/**
 * Restricts an "any target" (creature, planeswalker, or player) spell/ability. The
 * {@code permanentPredicate} is applied to creature/planeswalker targets and the
 * {@code playerPredicate} to player targets — both describing the same restriction
 * (e.g. "that was dealt damage this turn" for Needle Drop). Use this instead of a
 * {@link PermanentPredicateTargetFilter} or {@link PlayerPredicateTargetFilter} when the
 * restriction must apply across both target kinds of an any-target effect.
 */
public record AnyTargetPredicateTargetFilter(PermanentPredicate permanentPredicate,
                                             PlayerPredicate playerPredicate,
                                             String errorMessage) implements TargetFilter {
}
