package com.github.laxika.magicalvibes.model.condition;

/** The permanent that caused the trigger has greater power than the source permanent. */
public record TriggeringPermanentPowerGreaterThanSourcePower() implements Condition {

    @Override
    public String conditionName() {
        return "triggering permanent has greater power than source";
    }

    @Override
    public String conditionNotMetReason() {
        return "triggering permanent does not have greater power than source";
    }
}
