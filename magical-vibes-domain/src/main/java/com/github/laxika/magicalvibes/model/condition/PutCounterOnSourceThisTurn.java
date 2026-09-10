package com.github.laxika.magicalvibes.model.condition;

/**
 * True when the controller put a counter on the source permanent during the current turn.
 */
public record PutCounterOnSourceThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "put a counter on this permanent this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "you did not put a counter on this permanent this turn";
    }
}
