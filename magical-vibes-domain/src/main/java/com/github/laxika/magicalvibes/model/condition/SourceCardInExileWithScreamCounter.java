package com.github.laxika.magicalvibes.model.condition;

/**
 * The source card is in exile with a scream counter on it: "if this card is exiled with a scream counter on it"
 * (All Hallow's Eve).
 */
public record SourceCardInExileWithScreamCounter() implements Condition {

    @Override
    public String conditionName() {
        return "source card is in exile with a scream counter";
    }

    @Override
    public String conditionNotMetReason() {
        return "source card is not in exile with a scream counter";
    }
}
