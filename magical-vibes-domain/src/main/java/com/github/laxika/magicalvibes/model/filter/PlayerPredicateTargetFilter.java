package com.github.laxika.magicalvibes.model.filter;


public record PlayerPredicateTargetFilter(PlayerPredicate predicate, String errorMessage) implements TargetFilter {
}
