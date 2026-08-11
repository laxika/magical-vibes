package com.github.laxika.magicalvibes.model.condition;

/** It is the source controller's precombat or postcombat main phase. */
public record ControllerMainPhase() implements Condition {

    @Override
    public String conditionName() {
        return "controller's main phase";
    }

    @Override
    public String conditionNotMetReason() {
        return "not controller's main phase";
    }
}
