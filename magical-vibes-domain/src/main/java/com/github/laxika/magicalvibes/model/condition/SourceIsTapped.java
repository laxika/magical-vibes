package com.github.laxika.magicalvibes.model.condition;

/** The source permanent is tapped. */
public record SourceIsTapped() implements Condition {

    @Override
    public String conditionName() {
        return "source tapped";
    }

    @Override
    public String conditionNotMetReason() {
        return "source is untapped";
    }
}
