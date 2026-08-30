package com.github.laxika.magicalvibes.model.filter;


public record PermanentPredicateTargetFilter(
        PermanentPredicate predicate,
        String errorMessage,
        PermanentPredicate kickedPredicate,
        boolean activePlayerChoosesTarget
) implements TargetFilter {

    public PermanentPredicateTargetFilter(PermanentPredicate predicate, String errorMessage) {
        this(predicate, errorMessage, null, false);
    }

    public PermanentPredicateTargetFilter(PermanentPredicate predicate, String errorMessage,
                                          PermanentPredicate kickedPredicate) {
        this(predicate, errorMessage, kickedPredicate, false);
    }

    public PermanentPredicateTargetFilter(PermanentPredicate predicate, String errorMessage,
                                          boolean activePlayerChoosesTarget) {
        this(predicate, errorMessage, null, activePlayerChoosesTarget);
    }

    public PermanentPredicate predicateFor(boolean kicked) {
        return kicked && kickedPredicate != null ? kickedPredicate : predicate;
    }
}
