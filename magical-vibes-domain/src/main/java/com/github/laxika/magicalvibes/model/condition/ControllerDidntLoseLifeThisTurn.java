package com.github.laxika.magicalvibes.model.condition;

/** Matches when the source permanent's controller has not lost life during the current turn. */
public record ControllerDidntLoseLifeThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "you didn't lose life this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "you lost life this turn";
    }
}
