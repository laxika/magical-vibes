package com.github.laxika.magicalvibes.model.condition;

/** Coven: the controller controls three or more creatures with different powers. */
public record Coven() implements Condition {

    @Override
    public String conditionName() {
        return "coven";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than three creatures with different powers";
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
