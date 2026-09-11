package com.github.laxika.magicalvibes.model.condition;

/** The source permanent is attached to another permanent. */
public record SourceIsAttached() implements Condition {

    @Override
    public String conditionName() {
        return "source attached";
    }

    @Override
    public String conditionNotMetReason() {
        return "source is unattached";
    }
}
