package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.Set;

public record PermanentHasAnySubtypePredicate(Set<CardSubtype> subtypes) implements PermanentPredicate {
}
