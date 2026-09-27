package com.github.laxika.magicalvibes.model.filter;

/** Matches an ability whose source permanent has the subtype chosen by the evaluating source. */
public record StackEntryHasSourceChosenSubtypePredicate() implements StackEntryPredicate {
}
