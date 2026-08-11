package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CardSupertype;

import java.util.Set;

public record StackEntrySupertypeInPredicate(Set<CardSupertype> supertypes) implements StackEntryPredicate {
}
