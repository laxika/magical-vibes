package com.github.laxika.magicalvibes.model.condition;

/** The controller turned a permanent face up this turn. */
public record PermanentTurnedFaceUpThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "you turned a permanent face up this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "you didn't turn a permanent face up this turn";
    }
}
