package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.TargetFilter;

public record ControlledPermanentPredicateTargetFilter(PermanentPredicate predicate, String errorMessage) implements TargetFilter {
}
