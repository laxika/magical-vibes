package com.github.laxika.magicalvibes.model.condition;

/** The source card is currently in exile and has a fetch counter. */
public record SourceCardInExileWithFetchCounter() implements Condition {

    @Override
    public String conditionName() {
        return "source card is in exile with a fetch counter";
    }

    @Override
    public String conditionNotMetReason() {
        return "source card is not in exile with a fetch counter";
    }
}
