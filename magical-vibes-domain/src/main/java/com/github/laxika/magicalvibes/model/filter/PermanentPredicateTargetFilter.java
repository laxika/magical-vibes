package com.github.laxika.magicalvibes.model.filter;


public record PermanentPredicateTargetFilter(PermanentPredicate predicate, String errorMessage) implements TargetFilter {
}
