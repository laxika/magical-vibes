package com.github.laxika.magicalvibes.model.condition;

/** The source permanent has the suspected designation. */
public record SourceIsSuspected() implements Condition {

    @Override
    public String conditionName() {
        return "suspected";
    }

    @Override
    public String conditionNotMetReason() {
        return "source is not suspected";
    }
}
