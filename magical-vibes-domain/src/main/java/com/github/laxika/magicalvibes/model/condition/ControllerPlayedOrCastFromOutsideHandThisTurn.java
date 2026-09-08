package com.github.laxika.magicalvibes.model.condition;

/** True when the controller played a land or cast a spell from somewhere other than their hand this turn. */
public record ControllerPlayedOrCastFromOutsideHandThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "the controller played a land or cast a spell from outside their hand this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "the controller has not played a land or cast a spell from outside their hand this turn";
    }
}
