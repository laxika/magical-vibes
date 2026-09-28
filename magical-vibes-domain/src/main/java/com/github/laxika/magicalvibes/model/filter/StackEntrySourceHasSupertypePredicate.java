package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CardSupertype;

/** Matches an ability whose source permanent currently has the given supertype. */
public record StackEntrySourceHasSupertypePredicate(CardSupertype supertype) implements StackEntryPredicate {
}
