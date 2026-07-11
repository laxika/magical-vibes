package com.github.laxika.magicalvibes.model.condition;

/** Infusion condition: the effect's controller gained life this turn. */
public record GainedLifeThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "infusion";
    }

    @Override
    public String conditionNotMetReason() {
        return "you didn't gain life this turn";
    }
}
