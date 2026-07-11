package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The controller controls at least one permanent matching the predicate, excluding
 * the source permanent itself.
 */
public record ControlsAnotherPermanent(PermanentPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "controls another matching permanent";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller does not control another matching permanent";
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
