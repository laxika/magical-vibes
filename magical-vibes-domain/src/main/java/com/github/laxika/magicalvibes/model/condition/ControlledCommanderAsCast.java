package com.github.laxika.magicalvibes.model.condition;

/** Whether the spell's controller controlled a registered commander when the spell was finished being cast. */
public record ControlledCommanderAsCast() implements Condition {

    @Override
    public String conditionName() {
        return "controlled a commander as cast";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller did not control a commander as the spell was cast";
    }
}
