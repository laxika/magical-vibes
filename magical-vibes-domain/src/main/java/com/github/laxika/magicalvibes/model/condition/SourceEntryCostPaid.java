package com.github.laxika.magicalvibes.model.condition;

/** True when the source permanent paid its optional entry cost. */
public record SourceEntryCostPaid() implements Condition {

    @Override
    public String conditionName() {
        return "the entry cost was paid";
    }

    @Override
    public String conditionNotMetReason() {
        return "the entry cost was not paid";
    }
}
