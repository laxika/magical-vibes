package com.github.laxika.magicalvibes.model.condition;

/** The resolving spell was cast by paying an alternate cost instead of its mana cost. */
public record CastForAlternateCost() implements Condition {

    @Override
    public String conditionName() {
        return "alternate cost paid";
    }

    @Override
    public String conditionNotMetReason() {
        return "alternate cost was not paid";
    }
}
