package com.github.laxika.magicalvibes.model.condition;

/** The effect's controller sacrificed a permanent this turn. */
public record ControllerSacrificedPermanentThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "you sacrificed a permanent this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "you didn't sacrifice a permanent this turn";
    }
}
