package com.github.laxika.magicalvibes.model.filter;

/** Matches stack entries that were put onto the stack as copies rather than cast. */
public record StackEntryIsCopyPredicate() implements StackEntryPredicate {
}
