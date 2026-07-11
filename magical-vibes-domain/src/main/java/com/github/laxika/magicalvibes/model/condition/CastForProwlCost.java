package com.github.laxika.magicalvibes.model.condition;

/** The spell was cast for its prowl cost (CR 702.75), e.g. Latchkey Faerie's "if its prowl cost was paid". */
public record CastForProwlCost() implements Condition {

    @Override
    public String conditionName() {
        return "prowl cost paid";
    }

    @Override
    public String conditionNotMetReason() {
        return "prowl cost was not paid";
    }
}
