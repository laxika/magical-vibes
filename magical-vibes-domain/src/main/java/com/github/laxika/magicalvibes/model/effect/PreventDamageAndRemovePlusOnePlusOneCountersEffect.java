package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "If damage would be dealt to this creature, prevent that damage
 * and remove +1/+1 counters from it."
 * <p>
 * When {@code removeOneOnly} is {@code false} (default), removes counters equal to the damage
 * amount (e.g. Protean Hydra). When {@code true}, removes exactly one +1/+1 counter per damage
 * event regardless of the amount (e.g. Unbreathing Horde).
 *
 * @param removeOneOnly if true, remove exactly one counter per damage event instead of one per point
 */
public record PreventDamageAndRemovePlusOnePlusOneCountersEffect(boolean removeOneOnly) implements CardEffect {

    /** Default constructor: removes counters equal to damage (Protean Hydra behavior). */
    public PreventDamageAndRemovePlusOnePlusOneCountersEffect() {
        this(false);
    }
}
