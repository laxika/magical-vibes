package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.Objects;

/**
 * The resolution half of the keyword dies-returns: persist (CR 702.79a, a -1/-1 counter, the no-argument form) and
 * undying (CR 702.93a, a +1/+1 counter, {@link #undying()}): "return it to the battlefield under its owner's control
 * with a [counter] counter on it."
 *
 * <p>The "if it had no [counter] counters on it" intervening if is judged as the ability is collected, on the
 * creature as it last existed. On resolution the dying card is returned only from the graveyard stay its death put it
 * in (CR 400.7), with one {@link #counterType()} counter through the counter replacements.
 */
public record PersistReturnEffect(CounterType counterType) implements CardEffect {

    public PersistReturnEffect {
        Objects.requireNonNull(counterType, "counterType");
    }

    /** Persist: returns with a -1/-1 counter. */
    public PersistReturnEffect() {
        this(CounterType.MINUS_ONE_MINUS_ONE);
    }

    /** Undying: returns with a +1/+1 counter. */
    public static PersistReturnEffect undying() {
        return new PersistReturnEffect(CounterType.PLUS_ONE_PLUS_ONE);
    }

    /** The keyword this return belongs to, as the log names it. */
    public String keywordName() {
        return counterType == CounterType.PLUS_ONE_PLUS_ONE ? "undying" : "persist";
    }
}
