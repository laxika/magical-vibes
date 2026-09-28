package com.github.laxika.magicalvibes.model.condition;

/** True when the player associated with the current stack entry has one or more rad counters. */
public record TargetPlayerHasRadCounters() implements Condition {

    @Override
    public String conditionName() {
        return "that player has rad counters";
    }

    @Override
    public String conditionNotMetReason() {
        return "that player has no rad counters";
    }
}
