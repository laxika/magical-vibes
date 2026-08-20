package com.github.laxika.magicalvibes.model.condition;

/** The spell was cast using its foretell cost. */
public record CastForForetellCost() implements Condition {

    @Override
    public String conditionName() {
        return "foretell cost paid";
    }

    @Override
    public String conditionNotMetReason() {
        return "foretell cost was not paid";
    }
}
