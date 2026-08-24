package com.github.laxika.magicalvibes.model.condition;

/** The permanent or spell was cast for its spectacle cost. */
public record CastForSpectacleCost() implements Condition {

    @Override
    public String conditionName() {
        return "spectacle cost paid";
    }

    @Override
    public String conditionNotMetReason() {
        return "spectacle cost was not paid";
    }
}
