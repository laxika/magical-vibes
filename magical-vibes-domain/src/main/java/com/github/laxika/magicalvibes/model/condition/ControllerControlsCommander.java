package com.github.laxika.magicalvibes.model.condition;

/** Whether the controller currently controls one of their designated commanders. */
public record ControllerControlsCommander() implements Condition {

    @Override
    public String conditionName() {
        return "control a commander";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller does not control a commander";
    }
}
