package com.github.laxika.magicalvibes.model.condition;

/** True if the source permanent was declared as an attacker this turn. */
public record SourceAttackedThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "source attacked this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "this permanent didn't attack this turn";
    }
}
