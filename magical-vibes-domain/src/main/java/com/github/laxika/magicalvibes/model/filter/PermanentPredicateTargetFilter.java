package com.github.laxika.magicalvibes.model.filter;


public record PermanentPredicateTargetFilter(
        PermanentPredicate predicate,
        String errorMessage,
        PermanentPredicate kickedPredicate,
        PermanentPredicate giftPredicate,
        String giftErrorMessage,
        boolean activePlayerChoosesTarget,
        PermanentPredicate teamworkPredicate,
        String teamworkErrorMessage
) implements TargetFilter {

    public PermanentPredicateTargetFilter(PermanentPredicate predicate, String errorMessage) {
        this(predicate, errorMessage, null, null, null, false, null, null);
    }

    public PermanentPredicateTargetFilter(PermanentPredicate predicate, String errorMessage,
                                           PermanentPredicate kickedPredicate) {
        this(predicate, errorMessage, kickedPredicate, null, null, false, null, null);
    }

    public PermanentPredicateTargetFilter(PermanentPredicate predicate, String errorMessage,
                                          boolean activePlayerChoosesTarget) {
        this(predicate, errorMessage, null, null, null, activePlayerChoosesTarget, null, null);
    }

    public PermanentPredicateTargetFilter(PermanentPredicate predicate, String errorMessage,
                                          PermanentPredicate kickedPredicate,
                                          boolean activePlayerChoosesTarget) {
        this(predicate, errorMessage, kickedPredicate, null, null, activePlayerChoosesTarget, null, null);
    }

    public PermanentPredicateTargetFilter(PermanentPredicate predicate, String errorMessage,
                                          PermanentPredicate kickedPredicate,
                                          PermanentPredicate giftPredicate,
                                          String giftErrorMessage) {
        this(predicate, errorMessage, kickedPredicate, giftPredicate, giftErrorMessage, false, null, null);
    }

    public PermanentPredicateTargetFilter(PermanentPredicate predicate, String errorMessage,
                                          PermanentPredicate teamworkPredicate,
                                          String teamworkErrorMessage) {
        this(predicate, errorMessage, null, null, null, false, teamworkPredicate, teamworkErrorMessage);
    }

    public PermanentPredicateTargetFilter(PermanentPredicate predicate, String errorMessage,
                                          PermanentPredicate kickedPredicate,
                                          PermanentPredicate giftPredicate,
                                          String giftErrorMessage,
                                          boolean activePlayerChoosesTarget) {
        this(predicate, errorMessage, kickedPredicate, giftPredicate, giftErrorMessage,
                activePlayerChoosesTarget, null, null);
    }

    public PermanentPredicate predicateFor(boolean kicked) {
        return kicked && kickedPredicate != null ? kickedPredicate : predicate;
    }

    public PermanentPredicate predicateFor(boolean kicked, boolean giftPromised) {
        return giftPromised && giftPredicate != null ? giftPredicate : predicateFor(kicked);
    }

    public PermanentPredicate predicateFor(boolean kicked, boolean giftPromised, boolean teamworkCostPaid) {
        if (giftPromised && giftPredicate != null) {
            return giftPredicate;
        }
        if (teamworkCostPaid && teamworkPredicate != null) {
            return teamworkPredicate;
        }
        return predicateFor(kicked);
    }

    public String errorMessageFor(boolean giftPromised) {
        return giftPromised && giftErrorMessage != null ? giftErrorMessage : errorMessage;
    }

    public String errorMessageFor(boolean giftPromised, boolean teamworkCostPaid) {
        if (giftPromised && giftErrorMessage != null) {
            return giftErrorMessage;
        }
        return teamworkCostPaid && teamworkErrorMessage != null ? teamworkErrorMessage : errorMessage;
    }
}
