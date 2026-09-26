package com.github.laxika.magicalvibes.model.condition;

/** The permanent that caused the trigger has greater power than every other creature. */
public record TriggeringPermanentPowerGreaterThanEachOtherCreature() implements Condition {

    @Override
    public String conditionName() {
        return "triggering permanent has greater power than each other creature";
    }

    @Override
    public String conditionNotMetReason() {
        return "triggering permanent is not greater than each other creature";
    }
}
