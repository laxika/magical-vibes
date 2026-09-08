package com.github.laxika.magicalvibes.model.condition;

/** The spell's teamwork additional cost was paid. */
public record TeamworkCostPaid() implements Condition {

    @Override
    public String conditionName() {
        return "teamwork cost paid";
    }

    @Override
    public String conditionNotMetReason() {
        return "the teamwork cost was not paid";
    }
}
