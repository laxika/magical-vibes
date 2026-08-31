package com.github.laxika.magicalvibes.model.condition;

/** True when the controller controls fewer creatures than every opponent. */
public record ControllerControlsFewerCreaturesThanEachOpponent() implements Condition {

    @Override
    public String conditionName() {
        return "you control fewer creatures than each opponent";
    }

    @Override
    public String conditionNotMetReason() {
        return "you do not control fewer creatures than each opponent";
    }
}
