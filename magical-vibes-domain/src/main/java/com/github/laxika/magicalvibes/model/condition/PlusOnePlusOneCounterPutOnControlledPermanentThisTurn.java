package com.github.laxika.magicalvibes.model.condition;

/**
 * True when a +1/+1 counter was put on a permanent controlled by the condition's controller
 * during the current turn.
 */
public record PlusOnePlusOneCounterPutOnControlledPermanentThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "a +1/+1 counter was put on a permanent you control this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "a +1/+1 counter was not put on a permanent you control this turn";
    }
}
