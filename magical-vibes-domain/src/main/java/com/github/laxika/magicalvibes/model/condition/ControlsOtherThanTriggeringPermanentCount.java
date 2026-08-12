package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The controller controls at least {@code minCount} matching permanents, excluding both the
 * source permanent and the permanent that caused the enter-the-battlefield trigger.
 */
public record ControlsOtherThanTriggeringPermanentCount(int minCount, PermanentPredicate filter)
        implements Condition {

    @Override
    public String conditionName() {
        return "controls " + minCount + " or more matching permanents other than the trigger source";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller controls fewer than " + minCount + " matching permanents other than the trigger source";
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
