package com.github.laxika.magicalvibes.model.condition;

/** The game has the night designation. */
public record IsNight() implements Condition {

    @Override
    public String conditionName() {
        return "night";
    }

    @Override
    public String conditionNotMetReason() {
        return "it isn't night";
    }
}
