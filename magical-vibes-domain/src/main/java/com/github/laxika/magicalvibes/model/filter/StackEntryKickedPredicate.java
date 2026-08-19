package com.github.laxika.magicalvibes.model.filter;

/** Matches spells that were kicked, including spells with at least one multikicker payment. */
public record StackEntryKickedPredicate() implements StackEntryPredicate {
}
