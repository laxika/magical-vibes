package com.github.laxika.magicalvibes.model.filter;


public record OwnedPermanentPredicateTargetFilter(PermanentPredicate predicate, String errorMessage) implements TargetFilter {
}
