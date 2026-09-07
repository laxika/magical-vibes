package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a spell on the stack whose target occurrences all identify the same creature.
 * Multiple occurrences of that creature are allowed.
 */
public record StackEntryTargetsOnlySingleCreaturePredicate() implements StackEntryPredicate {
}
