package com.github.laxika.magicalvibes.model.filter;

/** Restricts a target group to face-up cards in exile matching the supplied predicate. */
public record ExiledCardPredicateTargetFilter(CardPredicate predicate, String errorMessage)
        implements TargetFilter {
}
