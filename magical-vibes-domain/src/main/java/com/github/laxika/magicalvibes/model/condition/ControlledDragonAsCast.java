package com.github.laxika.magicalvibes.model.condition;

/** Whether the spell's controller controlled a Dragon when the spell was finished being cast. */
public record ControlledDragonAsCast() implements Condition {

    @Override
    public String conditionName() {
        return "controlled a Dragon as cast";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller did not control a Dragon as the spell was cast";
    }
}
