package com.github.laxika.magicalvibes.model.condition;

/** Whether the spell's controller controlled a Faerie when the spell was finished being cast. */
public record ControlledFaerieAsCast() implements Condition {

    @Override
    public String conditionName() {
        return "controlled a Faerie as cast";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller did not control a Faerie as the spell was cast";
    }
}
