package com.github.laxika.magicalvibes.model.condition;

/** True when the permanent referenced by a triggered ability has effective power at least the threshold. */
public record TriggeringPermanentPowerAtLeast(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "triggering permanent's power is " + threshold + " or greater";
    }

    @Override
    public String conditionNotMetReason() {
        return "the triggering permanent's power is less than " + threshold;
    }
}
