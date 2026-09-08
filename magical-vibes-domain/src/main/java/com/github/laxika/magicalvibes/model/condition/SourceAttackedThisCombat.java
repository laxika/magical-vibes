package com.github.laxika.magicalvibes.model.condition;

/** True if the source permanent was declared as an attacker in the current combat. */
public record SourceAttackedThisCombat() implements Condition {

    @Override
    public String conditionName() {
        return "source attacked this combat";
    }

    @Override
    public String conditionNotMetReason() {
        return "this creature didn't attack this combat";
    }
}
