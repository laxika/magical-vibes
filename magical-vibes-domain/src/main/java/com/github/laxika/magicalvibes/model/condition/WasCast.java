package com.github.laxika.magicalvibes.model.condition;

/** The permanent entered the battlefield by resolving as a spell that was cast. */
public record WasCast() implements Condition {

    @Override
    public String conditionName() {
        return "was cast";
    }

    @Override
    public String conditionNotMetReason() {
        return "it was not cast";
    }
}
