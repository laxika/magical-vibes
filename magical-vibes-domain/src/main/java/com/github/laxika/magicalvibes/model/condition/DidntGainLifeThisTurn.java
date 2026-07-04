package com.github.laxika.magicalvibes.model.condition;

/** Infusion downside condition: the effect's controller did NOT gain life this turn. */
public record DidntGainLifeThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "infusion";
    }

    @Override
    public String conditionNotMetReason() {
        return "you gained life this turn";
    }
}
