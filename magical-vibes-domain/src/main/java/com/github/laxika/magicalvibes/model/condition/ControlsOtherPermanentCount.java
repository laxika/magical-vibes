package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The controller controls at least {@code minCount} permanents matching the predicate, excluding
 * the source permanent itself. The count analogue of {@link ControlsAnotherPermanent} — e.g. the
 * Amonkhet gods' "unless you control at least three other creatures".
 */
public record ControlsOtherPermanentCount(int minCount, PermanentPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "controls " + minCount + " or more other matching permanents";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller controls fewer than " + minCount + " other matching permanents";
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
