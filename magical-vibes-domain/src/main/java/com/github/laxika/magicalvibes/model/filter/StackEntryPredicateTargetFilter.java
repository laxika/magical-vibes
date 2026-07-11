package com.github.laxika.magicalvibes.model.filter;


public record StackEntryPredicateTargetFilter(StackEntryPredicate predicate, String errorMessage) implements TargetFilter {
}
