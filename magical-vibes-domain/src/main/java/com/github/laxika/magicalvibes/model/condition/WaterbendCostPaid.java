package com.github.laxika.magicalvibes.model.condition;

/** The spell's waterbend additional cost was paid. */
public record WaterbendCostPaid() implements Condition {

    @Override
    public String conditionName() {
        return "waterbend cost paid";
    }

    @Override
    public String conditionNotMetReason() {
        return "the waterbend cost was not paid";
    }
}
