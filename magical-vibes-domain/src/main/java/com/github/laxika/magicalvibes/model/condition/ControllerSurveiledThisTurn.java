package com.github.laxika.magicalvibes.model.condition;

/** The controller has surveilled at least once this turn. */
public record ControllerSurveiledThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "surveiled this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "you haven't surveilled this turn";
    }
}
