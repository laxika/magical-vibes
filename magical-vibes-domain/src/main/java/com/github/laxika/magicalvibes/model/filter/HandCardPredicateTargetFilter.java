package com.github.laxika.magicalvibes.model.filter;

/** Restricts a target group to cards in the controller's hand. */
public record HandCardPredicateTargetFilter(CardPredicate predicate, String errorMessage)
        implements TargetFilter {
}
