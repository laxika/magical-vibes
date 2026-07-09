package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.Set;

/**
 * Matches spells whose card has at least one of the given subtypes (e.g. Faerie). Combine with
 * {@link StackEntryNotPredicate} for "non-[subtype]" targeting restrictions.
 */
public record StackEntrySubtypeInPredicate(Set<CardSubtype> subtypes) implements StackEntryPredicate {
}
