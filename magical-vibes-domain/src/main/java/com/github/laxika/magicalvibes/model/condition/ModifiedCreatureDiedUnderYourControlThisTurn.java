package com.github.laxika.magicalvibes.model.condition;

/** A modified creature died under the effect controller's control this turn. */
public record ModifiedCreatureDiedUnderYourControlThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "a modified creature died under your control this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no modified creature died under your control this turn";
    }
}
