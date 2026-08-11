package com.github.laxika.magicalvibes.model.condition;

/** The current turn was created by an extra-turn effect. */
public record ExtraTurn() implements Condition {

    @Override
    public String conditionName() {
        return "an extra turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "a normal turn";
    }
}
