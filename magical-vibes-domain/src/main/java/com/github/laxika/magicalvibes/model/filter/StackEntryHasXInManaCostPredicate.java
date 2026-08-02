package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches spells on the stack whose mana cost contains an X symbol, regardless of the chosen X.
 */
public record StackEntryHasXInManaCostPredicate() implements StackEntryPredicate {
}
