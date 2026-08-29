package com.github.laxika.magicalvibes.model.condition;

/** The resolving spell was cast for its madness cost. */
public record CastForMadnessCost() implements Condition {

    @Override
    public String conditionName() {
        return "madness cost paid";
    }

    @Override
    public String conditionNotMetReason() {
        return "madness cost was not paid";
    }
}
