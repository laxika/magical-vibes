package com.github.laxika.magicalvibes.model.condition;

/** Whether the spell's controller controlled a Mount when the spell was finished being cast. */
public record ControlledMountAsCast() implements Condition {

    @Override
    public String conditionName() {
        return "controlled a Mount as cast";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller did not control a Mount as the spell was cast";
    }
}
