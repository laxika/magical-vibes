package com.github.laxika.magicalvibes.model.condition;

/** Morbid: a creature died this turn. */
public record Morbid() implements Condition {

    @Override
    public String conditionName() {
        return "morbid";
    }

    @Override
    public String conditionNotMetReason() {
        return "no creature died this turn";
    }
}
