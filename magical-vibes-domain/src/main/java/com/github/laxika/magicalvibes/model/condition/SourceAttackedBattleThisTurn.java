package com.github.laxika.magicalvibes.model.condition;

/** True if the source permanent was declared as an attacker against a battle this turn. */
public record SourceAttackedBattleThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "source attacked a battle this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "this creature did not attack a battle this turn";
    }
}
