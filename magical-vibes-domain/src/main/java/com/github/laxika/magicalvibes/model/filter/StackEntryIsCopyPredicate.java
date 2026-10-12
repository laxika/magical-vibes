package com.github.laxika.magicalvibes.model.filter;

/** Matches stack entries that were put onto the stack as copies and were not cast. */
public record StackEntryIsCopyPredicate() implements StackEntryPredicate {
}
