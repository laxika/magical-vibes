package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** The controller controls at least one permanent matching the predicate. */
public record ControlsPermanent(PermanentPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "controls a matching permanent";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller does not control a matching permanent";
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
