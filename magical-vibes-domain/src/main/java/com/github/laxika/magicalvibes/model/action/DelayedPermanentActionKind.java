package com.github.laxika.magicalvibes.model.action;

/**
 * The zone-change operation and timing point of a {@link DelayedPermanentAction}. Each constant
 * pairs the operation applied at drain time with the player-visible game-log suffix for that
 * flavour ("token is exiled" vs "is exiled", end-step vs end-of-combat destroy wording), so the
 * log output stays per-kind while one drain loop services them all.
 */
public enum DelayedPermanentActionKind {

    EXILE_TOKEN_AT_END_STEP(Op.EXILE, " token is exiled."),
    EXILE_AT_END_STEP(Op.EXILE, " is exiled."),
    SACRIFICE_AT_END_STEP(Op.SACRIFICE, " is sacrificed."),
    DESTROY_AT_END_STEP(Op.DESTROY, " is destroyed at end step."),
    RETURN_TO_HAND_AT_END_STEP(Op.RETURN_TO_HAND, " is returned to its owner's hand."),
    EXILE_TOKEN_AT_END_OF_COMBAT(Op.EXILE, " token is exiled."),
    DESTROY_AT_END_OF_COMBAT(Op.DESTROY, " is destroyed.");

    /** The zone-change operation the drain loop applies to the scheduled permanent. */
    public enum Op { EXILE, SACRIFICE, DESTROY, RETURN_TO_HAND }

    private final Op op;
    private final String logSuffix;

    DelayedPermanentActionKind(Op op, String logSuffix) {
        this.op = op;
        this.logSuffix = logSuffix;
    }

    public Op op() {
        return op;
    }

    public String logSuffix() {
        return logSuffix;
    }
}
