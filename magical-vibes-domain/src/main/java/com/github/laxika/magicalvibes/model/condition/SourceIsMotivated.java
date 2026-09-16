package com.github.laxika.magicalvibes.model.condition;

/** The source permanent has been motivated. */
public record SourceIsMotivated() implements Condition {

    @Override
    public String conditionName() {
        return "motivated";
    }

    @Override
    public String conditionNotMetReason() {
        return "source has not been motivated";
    }
}
