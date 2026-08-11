package com.github.laxika.magicalvibes.model.filter;


public record StackEntryPredicateTargetFilter(
        StackEntryPredicate predicate,
        String errorMessage,
        StackEntryPredicate kickedPredicate
) implements TargetFilter {

    public StackEntryPredicateTargetFilter(StackEntryPredicate predicate, String errorMessage) {
        this(predicate, errorMessage, null);
    }

    public StackEntryPredicate predicateFor(boolean kicked) {
        return kicked && kickedPredicate != null ? kickedPredicate : predicate;
    }
}
