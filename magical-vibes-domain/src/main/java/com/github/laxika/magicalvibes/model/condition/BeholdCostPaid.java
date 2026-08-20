package com.github.laxika.magicalvibes.model.condition;

/** The spell's optional behold additional cost was paid. */
public record BeholdCostPaid() implements Condition {

    @Override
    public String conditionName() {
        return "behold cost paid";
    }

    @Override
    public String conditionNotMetReason() {
        return "the behold cost was not paid";
    }
}
