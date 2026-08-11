package com.github.laxika.magicalvibes.model.condition;

/** The spell's put-counter additional cost was paid. */
public record PutCounterCostPaid() implements Condition {

    @Override
    public String conditionName() {
        return "put-counter cost paid";
    }

    @Override
    public String conditionNotMetReason() {
        return "the put-counter cost was not paid";
    }
}
