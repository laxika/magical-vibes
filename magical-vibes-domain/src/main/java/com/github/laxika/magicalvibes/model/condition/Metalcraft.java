package com.github.laxika.magicalvibes.model.condition;

/** Metalcraft: the controller controls three or more artifacts. */
public record Metalcraft() implements Condition {

    @Override
    public String conditionName() {
        return "metalcraft";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than three artifacts";
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
