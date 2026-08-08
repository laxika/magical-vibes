package com.github.laxika.magicalvibes.model.condition;

/** The source permanent is a token. Wrap in {@link NotCondition} for "if this isn't a token". */
public record SourceIsToken() implements Condition {

    @Override
    public String conditionName() {
        return "source is a token";
    }

    @Override
    public String conditionNotMetReason() {
        return "source is not a token";
    }
}
