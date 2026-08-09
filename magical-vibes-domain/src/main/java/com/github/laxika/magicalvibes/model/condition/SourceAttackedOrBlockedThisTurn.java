package com.github.laxika.magicalvibes.model.condition;

/** True if the source permanent was declared as an attacker or blocker this turn. */
public record SourceAttackedOrBlockedThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "source attacked or blocked this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "this creature didn't attack or block this turn";
    }
}
