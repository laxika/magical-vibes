package com.github.laxika.magicalvibes.model.condition;

/** The source creature has at least one Equipment attached. */
public record Equipped() implements Condition {

    @Override
    public String conditionName() {
        return "equipped";
    }

    @Override
    public String conditionNotMetReason() {
        return "not equipped";
    }
}
