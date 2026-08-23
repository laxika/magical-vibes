package com.github.laxika.magicalvibes.model.condition;

/** Intervening condition for an ability that requires its source card to remain suspended. */
public record SourceCardSuspended() implements Condition {

    @Override
    public String conditionName() {
        return "source suspended";
    }

    @Override
    public String conditionNotMetReason() {
        return "source is no longer suspended";
    }
}
