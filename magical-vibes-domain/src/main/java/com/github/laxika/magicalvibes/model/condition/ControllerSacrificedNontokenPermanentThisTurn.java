package com.github.laxika.magicalvibes.model.condition;

/** The effect's controller sacrificed a nontoken permanent this turn. */
public record ControllerSacrificedNontokenPermanentThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "you sacrificed a nontoken permanent this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "you didn't sacrifice a nontoken permanent this turn";
    }
}
