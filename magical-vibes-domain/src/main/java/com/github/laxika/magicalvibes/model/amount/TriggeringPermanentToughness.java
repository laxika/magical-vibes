package com.github.laxika.magicalvibes.model.amount;

/**
 * The effective toughness of the permanent that caused a trigger, evaluated when the trigger resolves. If that
 * permanent has left the battlefield, its last-known toughness is used (CR 608.2h). A negative toughness counts as 0
 * unless {@code allowNegative}: "base toughness becomes equal to that creature's toughness" copies a negative toughness
 * (CR 107.1b; Eldrazi Mimic).
 *
 * @param allowNegative whether a negative toughness is kept rather than counted as 0
 */
public record TriggeringPermanentToughness(boolean allowNegative) implements DynamicAmount {

    public TriggeringPermanentToughness() {
        this(false);
    }
}
