package com.github.laxika.magicalvibes.model.condition;

/** Any opponent of the controller has at least the requested number of poison counters. */
public record OpponentPoisoned(int minimumPoisonCounters) implements Condition {

    public OpponentPoisoned() {
        this(1);
    }

    @Override
    public String conditionName() {
        return "opponent has at least " + minimumPoisonCounters + " poison counters";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent has at least " + minimumPoisonCounters + " poison counters";
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
