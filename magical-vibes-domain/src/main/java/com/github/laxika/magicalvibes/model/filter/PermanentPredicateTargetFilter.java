package com.github.laxika.magicalvibes.model.filter;


public record PermanentPredicateTargetFilter(
        PermanentPredicate predicate,
        String errorMessage,
        PermanentPredicate kickedPredicate,
        PermanentPredicate giftPredicate,
        String giftErrorMessage,
        boolean activePlayerChoosesTarget
) implements TargetFilter {

    public PermanentPredicateTargetFilter(PermanentPredicate predicate, String errorMessage) {
        this(predicate, errorMessage, null, null, null, false);
    }

    public PermanentPredicateTargetFilter(PermanentPredicate predicate, String errorMessage,
                                           PermanentPredicate kickedPredicate) {
        this(predicate, errorMessage, kickedPredicate, null, null, false);
    }

    public PermanentPredicateTargetFilter(PermanentPredicate predicate, String errorMessage,
                                          boolean activePlayerChoosesTarget) {
        this(predicate, errorMessage, null, null, null, activePlayerChoosesTarget);
    }

    public PermanentPredicateTargetFilter(PermanentPredicate predicate, String errorMessage,
                                          PermanentPredicate kickedPredicate,
                                          boolean activePlayerChoosesTarget) {
        this(predicate, errorMessage, kickedPredicate, null, null, activePlayerChoosesTarget);
    }

    public PermanentPredicateTargetFilter(PermanentPredicate predicate, String errorMessage,
                                          PermanentPredicate kickedPredicate,
                                          PermanentPredicate giftPredicate,
                                          String giftErrorMessage) {
        this(predicate, errorMessage, kickedPredicate, giftPredicate, giftErrorMessage, false);
    }

    public PermanentPredicate predicateFor(boolean kicked) {
        return kicked && kickedPredicate != null ? kickedPredicate : predicate;
    }

    public PermanentPredicate predicateFor(boolean kicked, boolean giftPromised) {
        return giftPromised && giftPredicate != null ? giftPredicate : predicateFor(kicked);
    }

    public String errorMessageFor(boolean giftPromised) {
        return giftPromised && giftErrorMessage != null ? giftErrorMessage : errorMessage;
    }
}
