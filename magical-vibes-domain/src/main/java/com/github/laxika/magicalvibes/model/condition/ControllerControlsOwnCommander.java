package com.github.laxika.magicalvibes.model.condition;

/** Whether the controller currently controls a commander they own ("your commander"). */
public record ControllerControlsOwnCommander() implements Condition {

    @Override
    public String conditionName() {
        return "control your commander";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller does not control their commander";
    }
}
