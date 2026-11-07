package com.github.laxika.magicalvibes.model.effect;

/**
 * Doubles the number of +1/+1 counters on the source permanent, or, for
 * {@link #ofTriggeringPermanent()}, on the permanent the trigger event was about ("double the number
 * of +1/+1 counters on it", read from the stack entry's triggering permanent as the ability resolves).
 *
 * @param reference {@link PermanentReference#SOURCE} or {@link PermanentReference#TRIGGERING}
 */
public record DoublePlusOneCountersOnSourceEffect(PermanentReference reference) implements CardEffect, DoublingEffect {

    public DoublePlusOneCountersOnSourceEffect {
        if (reference != PermanentReference.SOURCE && reference != PermanentReference.TRIGGERING) {
            throw new IllegalArgumentException("Doubles the source's or the triggering permanent's counters: "
                    + reference);
        }
    }

    public DoublePlusOneCountersOnSourceEffect() {
        this(PermanentReference.SOURCE);
    }

    /** "Double the number of +1/+1 counters on it": the stack entry's triggering permanent. */
    public static DoublePlusOneCountersOnSourceEffect ofTriggeringPermanent() {
        return new DoublePlusOneCountersOnSourceEffect(PermanentReference.TRIGGERING);
    }
}
