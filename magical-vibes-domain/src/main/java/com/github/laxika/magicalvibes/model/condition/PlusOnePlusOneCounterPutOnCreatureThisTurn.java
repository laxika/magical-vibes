package com.github.laxika.magicalvibes.model.condition;

/**
 * True when the controller put at least one +1/+1 counter on a creature during the current turn.
 */
public record PlusOnePlusOneCounterPutOnCreatureThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "put a +1/+1 counter on a creature this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "you did not put a +1/+1 counter on a creature this turn";
    }
}
