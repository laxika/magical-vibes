package com.github.laxika.magicalvibes.model.condition;

/** The effect's controller created a token this turn. */
public record ControllerCreatedTokenThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "you created a token this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "you didn't create a token this turn";
    }
}
