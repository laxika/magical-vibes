package com.github.laxika.magicalvibes.model.condition;

/** A creature died under the effect controller's control this turn (controller-scoped morbid). */
public record CreatureDiedUnderYourControlThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "a creature died under your control this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no creature died under your control this turn";
    }
}
