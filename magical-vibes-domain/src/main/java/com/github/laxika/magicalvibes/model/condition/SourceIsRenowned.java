package com.github.laxika.magicalvibes.model.condition;

/** The source permanent is renowned (CR 702.112b). */
public record SourceIsRenowned() implements Condition {

    @Override
    public String conditionName() {
        return "renowned";
    }

    @Override
    public String conditionNotMetReason() {
        return "source is not renowned";
    }
}
