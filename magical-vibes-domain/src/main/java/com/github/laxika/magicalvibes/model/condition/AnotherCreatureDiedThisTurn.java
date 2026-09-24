package com.github.laxika.magicalvibes.model.condition;

/** A creature other than the targeted creature died this turn. */
public record AnotherCreatureDiedThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "another creature died this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no other creature died this turn";
    }
}
