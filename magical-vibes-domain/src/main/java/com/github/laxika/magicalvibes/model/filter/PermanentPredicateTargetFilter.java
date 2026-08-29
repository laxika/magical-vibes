package com.github.laxika.magicalvibes.model.filter;


public record PermanentPredicateTargetFilter(
        PermanentPredicate predicate,
        String errorMessage,
        PermanentPredicate kickedPredicate,
        PermanentPredicate giftPredicate,
        String giftErrorMessage
) implements TargetFilter {

    public PermanentPredicateTargetFilter(PermanentPredicate predicate, String errorMessage) {
        this(predicate, errorMessage, null, null, null);
    }

    public PermanentPredicateTargetFilter(PermanentPredicate predicate, String errorMessage,
                                          PermanentPredicate kickedPredicate) {
        this(predicate, errorMessage, kickedPredicate, null, null);
    }

    public PermanentPredicateTargetFilter(PermanentPredicate predicate, String errorMessage,
                                          PermanentPredicate giftPredicate, String giftErrorMessage) {
        this(predicate, errorMessage, null, giftPredicate, giftErrorMessage);
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

    public PermanentPredicateTargetFilter forCast(boolean kicked, boolean giftPromised) {
        return new PermanentPredicateTargetFilter(
                predicateFor(kicked, giftPromised), errorMessageFor(giftPromised));
    }
}
