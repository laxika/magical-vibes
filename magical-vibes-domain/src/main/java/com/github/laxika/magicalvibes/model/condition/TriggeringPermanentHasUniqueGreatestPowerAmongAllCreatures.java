package com.github.laxika.magicalvibes.model.condition;

/** The permanent that caused the trigger has strictly greater power than every other creature. */
public record TriggeringPermanentHasUniqueGreatestPowerAmongAllCreatures() implements Condition {

    @Override
    public String conditionName() {
        return "triggering permanent has unique greatest power";
    }

    @Override
    public String conditionNotMetReason() {
        return "triggering permanent is not strictly greater than each other creature";
    }
}
