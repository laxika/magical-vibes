package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches stack entries controlled by the player evaluating the predicate.
 * The controller identity is supplied externally (by the validation service);
 * this record is a marker only.
 */
public record StackEntryControlledByPredicate() implements StackEntryPredicate {
}
