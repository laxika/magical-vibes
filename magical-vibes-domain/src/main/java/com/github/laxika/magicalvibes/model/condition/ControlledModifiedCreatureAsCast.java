package com.github.laxika.magicalvibes.model.condition;

/** Whether the spell's controller controlled a modified creature when the spell was finished being cast. */
public record ControlledModifiedCreatureAsCast() implements Condition {

    @Override
    public String conditionName() {
        return "controlled a modified creature as cast";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller did not control a modified creature as the spell was cast";
    }
}
