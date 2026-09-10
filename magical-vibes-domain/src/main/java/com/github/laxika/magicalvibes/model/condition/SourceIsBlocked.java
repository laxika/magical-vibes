package com.github.laxika.magicalvibes.model.condition;

/** Whether the source is a blocked attacker, including when no creatures are blocking it. */
public record SourceIsBlocked() implements Condition {
    @Override
    public String conditionName() {
        return "source blocked";
    }

    @Override
    public String conditionNotMetReason() {
        return "source is not blocked";
    }
}
