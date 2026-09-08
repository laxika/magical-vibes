package com.github.laxika.magicalvibes.model.condition;

/** It is the source controller's end step. */
public record ControllerEndStep() implements Condition {

    @Override
    public String conditionName() {
        return "controller's end step";
    }

    @Override
    public String conditionNotMetReason() {
        return "not controller's end step";
    }
}
