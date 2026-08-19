package com.github.laxika.magicalvibes.model.condition;

/** True if the source permanent was declared as an attacker or blocker in the current combat. */
public record SourceAttackedOrBlockedThisCombat() implements Condition {

    @Override
    public String conditionName() {
        return "source attacked or blocked this combat";
    }

    @Override
    public String conditionNotMetReason() {
        return "this creature didn't attack or block this combat";
    }
}
