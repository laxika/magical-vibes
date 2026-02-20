package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.TargetFilter;

public record PermanentPredicateTargetFilter(PermanentPredicate predicate, String errorMessage) implements TargetFilter {
}
