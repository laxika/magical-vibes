package com.github.laxika.magicalvibes.model.condition;

/** True while the source permanent has face-down characteristics. */
public record SourceIsFaceDown() implements Condition {

    @Override
    public String conditionName() {
        return "face down";
    }

    @Override
    public String conditionNotMetReason() {
        return "the source is face up";
    }
}
