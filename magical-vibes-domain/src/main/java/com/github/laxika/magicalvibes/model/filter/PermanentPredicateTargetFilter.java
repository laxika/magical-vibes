package com.github.laxika.magicalvibes.model.filter;


public record PermanentPredicateTargetFilter(
        PermanentPredicate predicate,
        String errorMessage,
        PermanentPredicate kickedPredicate
) implements TargetFilter {

    public PermanentPredicateTargetFilter(PermanentPredicate predicate, String errorMessage) {
        this(predicate, errorMessage, null);
    }

    public PermanentPredicate predicateFor(boolean kicked) {
        return kicked && kickedPredicate != null ? kickedPredicate : predicate;
    }
}
