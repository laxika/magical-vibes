package com.github.laxika.magicalvibes.model.condition;

/**
 * True when the controller put at least one counter on a creature during the current turn.
 */
public record PutCounterOnCreatureThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "put a counter on a creature this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "you did not put a counter on a creature this turn";
    }
}
