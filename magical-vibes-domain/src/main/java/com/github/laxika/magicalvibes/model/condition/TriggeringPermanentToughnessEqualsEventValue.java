package com.github.laxika.magicalvibes.model.condition;

/**
 * True when the toughness of the permanent the trigger event names equals the event's value: as a damage trigger's
 * event condition, "deals damage to a creature equal to that creature's toughness" (Taii Wakeen, Perfect Shot), the
 * damage dealt against the creature's toughness as it was dealt (damage already marked on it doesn't matter).
 */
public record TriggeringPermanentToughnessEqualsEventValue() implements Condition {

    @Override
    public String conditionName() {
        return "the amount equals that permanent's toughness";
    }

    @Override
    public String conditionNotMetReason() {
        return "the amount doesn't equal that permanent's toughness";
    }
}
