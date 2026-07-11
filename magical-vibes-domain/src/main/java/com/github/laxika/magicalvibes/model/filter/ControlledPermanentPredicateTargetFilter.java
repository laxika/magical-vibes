package com.github.laxika.magicalvibes.model.filter;


public record ControlledPermanentPredicateTargetFilter(PermanentPredicate predicate, String errorMessage) implements TargetFilter {
}
