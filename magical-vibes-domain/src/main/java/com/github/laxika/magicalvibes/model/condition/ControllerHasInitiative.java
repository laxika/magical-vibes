package com.github.laxika.magicalvibes.model.condition;

/** The controller currently has the initiative designation. */
public record ControllerHasInitiative() implements Condition {

    @Override
    public String conditionName() {
        return "has the initiative";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller does not have the initiative";
    }
}
