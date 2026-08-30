package com.github.laxika.magicalvibes.model.condition;

public record AllBendingTypesCompletedThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "all four bending types";
    }

    @Override
    public String conditionNotMetReason() {
        return "not all four bending types have been completed this turn";
    }
}
