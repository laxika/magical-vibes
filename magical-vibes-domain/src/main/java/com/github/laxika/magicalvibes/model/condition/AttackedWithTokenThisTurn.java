package com.github.laxika.magicalvibes.model.condition;

/** The controller declared at least one token as an attacker this turn. */
public record AttackedWithTokenThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "attacked with a token this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "you didn't attack with a token this turn";
    }
}
