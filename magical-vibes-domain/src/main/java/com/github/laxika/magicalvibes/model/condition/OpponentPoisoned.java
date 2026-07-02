package com.github.laxika.magicalvibes.model.condition;

/** Any opponent of the controller has at least one poison counter. */
public record OpponentPoisoned() implements Condition {

    @Override
    public String conditionName() {
        return "opponent poisoned";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent is poisoned";
    }
}
