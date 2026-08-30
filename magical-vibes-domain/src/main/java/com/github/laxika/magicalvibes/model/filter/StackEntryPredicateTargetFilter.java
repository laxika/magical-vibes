package com.github.laxika.magicalvibes.model.filter;


public record StackEntryPredicateTargetFilter(
        StackEntryPredicate predicate,
        String errorMessage,
        StackEntryPredicate kickedPredicate,
        StackEntryPredicate giftPredicate,
        String giftErrorMessage
) implements TargetFilter {

    public StackEntryPredicateTargetFilter(StackEntryPredicate predicate, String errorMessage) {
        this(predicate, errorMessage, null, null, null);
    }

    public StackEntryPredicateTargetFilter(StackEntryPredicate predicate, String errorMessage,
                                           StackEntryPredicate kickedPredicate) {
        this(predicate, errorMessage, kickedPredicate, null, null);
    }

    public StackEntryPredicateTargetFilter(StackEntryPredicate predicate, String errorMessage,
                                           StackEntryPredicate giftPredicate, String giftErrorMessage) {
        this(predicate, errorMessage, null, giftPredicate, giftErrorMessage);
    }

    public StackEntryPredicate predicateFor(boolean kicked) {
        return kicked && kickedPredicate != null ? kickedPredicate : predicate;
    }

    public StackEntryPredicate predicateFor(boolean kicked, boolean giftPromised) {
        return giftPromised && giftPredicate != null ? giftPredicate : predicateFor(kicked);
    }

    public String errorMessageFor(boolean giftPromised) {
        return giftPromised && giftErrorMessage != null ? giftErrorMessage : errorMessage;
    }
}
