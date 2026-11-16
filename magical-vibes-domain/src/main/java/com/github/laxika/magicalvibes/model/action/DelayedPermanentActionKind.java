package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.trigger.StepTrigger;

/**
 * The zone-change operation and timing point of a {@link DelayedPermanentAction}. Each constant pairs the operation
 * the delayed triggered ability applies with the step that triggers it and the player-visible game-log suffix for that
 * flavour ("token is exiled" vs "is exiled", end-step vs end-of-combat destroy wording).
 */
public enum DelayedPermanentActionKind {

    EXILE_TOKEN_AT_END_STEP(Op.EXILE, StepTrigger.Moment.END_STEP, " token is exiled."),
    EXILE_AT_END_STEP(Op.EXILE, StepTrigger.Moment.END_STEP, " is exiled."),
    EXILE_WARPED_AT_END_STEP(Op.EXILE, StepTrigger.Moment.END_STEP, " is exiled."),
    SACRIFICE_AT_END_STEP(Op.SACRIFICE, StepTrigger.Moment.END_STEP, " is sacrificed."),
    SACRIFICE_AT_NEXT_UPKEEP(Op.SACRIFICE, StepTrigger.Moment.UPKEEP, " is sacrificed."),
    DESTROY_AT_END_STEP(Op.DESTROY, StepTrigger.Moment.END_STEP, " is destroyed at end step."),
    RETURN_TO_HAND_AT_END_STEP(Op.RETURN_TO_HAND, StepTrigger.Moment.END_STEP, " is returned to its owner's hand."),
    RETURN_TO_COMMAND_ZONE_AT_END_STEP(Op.RETURN_TO_COMMAND_ZONE, StepTrigger.Moment.END_STEP,
            " is returned to the command zone."),
    EXILE_TOKEN_AT_END_OF_COMBAT(Op.EXILE, StepTrigger.Moment.END_OF_COMBAT, " token is exiled."),
    SACRIFICE_AT_END_OF_COMBAT(Op.SACRIFICE, StepTrigger.Moment.END_OF_COMBAT, " is sacrificed."),
    DESTROY_AT_END_OF_COMBAT(Op.DESTROY, StepTrigger.Moment.END_OF_COMBAT, " is destroyed."),
    RETURN_TO_HAND_AT_END_OF_COMBAT(Op.RETURN_TO_HAND, StepTrigger.Moment.END_OF_COMBAT,
            " is returned to its owner's hand."),
    PUT_ON_TOP_OF_LIBRARY_AT_END_OF_COMBAT(Op.PUT_ON_TOP_OF_LIBRARY, StepTrigger.Moment.END_OF_COMBAT,
            " is put on top of its owner's library.");

    /** The zone-change operation the delayed triggered ability applies to the permanent. */
    public enum Op { EXILE, SACRIFICE, DESTROY, RETURN_TO_HAND, RETURN_TO_COMMAND_ZONE, PUT_ON_TOP_OF_LIBRARY }

    private final Op op;
    private final StepTrigger.Moment moment;
    private final String logSuffix;

    DelayedPermanentActionKind(Op op, StepTrigger.Moment moment, String logSuffix) {
        this.op = op;
        this.moment = moment;
        this.logSuffix = logSuffix;
    }

    public Op op() {
        return op;
    }

    /** The step or phase whose beginning triggers the delayed ability. */
    public StepTrigger.Moment moment() {
        return moment;
    }

    public String logSuffix() {
        return logSuffix;
    }
}
