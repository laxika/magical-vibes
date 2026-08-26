package com.github.laxika.magicalvibes.model.condition;

/** The source permanent is harnessed. */
public record SourceIsHarnessed() implements Condition {

    @Override
    public String conditionName() {
        return "harnessed";
    }

    @Override
    public String conditionNotMetReason() {
        return "source is not harnessed";
    }
}
