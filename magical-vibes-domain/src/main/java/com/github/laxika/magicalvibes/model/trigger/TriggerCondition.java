package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;

import java.util.Objects;

/**
 * One trigger condition of a triggered ability and the zones it functions from. The conditions of
 * one ability may function from different zones: "When this creature enters or the creature it
 * haunts dies" triggers from the battlefield and from exile (CR 113.6k).
 *
 * @param trigger the trigger event and its filters
 * @param zones   the zones the ability's object must be in for this condition to trigger
 */
public record TriggerCondition(Trigger trigger, AbilityZones zones) {

    public TriggerCondition {
        Objects.requireNonNull(trigger, "trigger");
        Objects.requireNonNull(zones, "zones");
    }

    /** A condition that functions from {@code trigger}'s default zones. */
    public TriggerCondition(Trigger trigger) {
        this(trigger, Objects.requireNonNull(trigger, "trigger").defaultZones());
    }
}
