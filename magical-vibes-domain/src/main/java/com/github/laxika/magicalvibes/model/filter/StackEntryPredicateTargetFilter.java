package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.TargetFilter;

public record StackEntryPredicateTargetFilter(StackEntryPredicate predicate, String errorMessage) implements TargetFilter {
}
