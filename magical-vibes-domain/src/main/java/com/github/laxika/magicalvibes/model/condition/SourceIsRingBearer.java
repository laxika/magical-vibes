package com.github.laxika.magicalvibes.model.condition;

/** The source permanent is its controller's Ring-bearer. */
public record SourceIsRingBearer() implements Condition {

    @Override
    public String conditionName() {
        return "Ring-bearer";
    }

    @Override
    public String conditionNotMetReason() {
        return "the source is not your Ring-bearer";
    }
}
