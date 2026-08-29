package com.github.laxika.magicalvibes.model.condition;

/** The source permanent is saddled until end of turn. */
public record SourceIsSaddled() implements Condition {

    @Override
    public String conditionName() {
        return "saddled";
    }

    @Override
    public String conditionNotMetReason() {
        return "source is not saddled";
    }
}
