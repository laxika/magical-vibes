package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.StackEntryType;

import java.util.Set;

public record StackEntryTypeInPredicate(Set<StackEntryType> spellTypes) implements StackEntryPredicate {
}
