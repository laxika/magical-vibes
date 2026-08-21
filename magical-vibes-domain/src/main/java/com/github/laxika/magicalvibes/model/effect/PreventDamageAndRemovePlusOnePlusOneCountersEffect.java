package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "If damage would be dealt to this creature, prevent that damage
 * and remove +1/+1 counters from it."
 * <p>
 * When {@code removeOneOnly} is {@code false} (default), removes counters equal to the damage
 * amount (e.g. Protean Hydra). When {@code true}, removes exactly one +1/+1 counter per damage
 * event regardless of the amount (e.g. Unbreathing Horde). When
 * {@code preventOnlyIfCounterAvailable} is {@code true}, only one damage is prevented for each
 * counter available (e.g. Rock Hydra).
 *
 * @param removeOneOnly if true, remove exactly one counter per damage event instead of one per point
 * @param tokenTemplate optional token blueprint created once per point of prevented damage
 * @param preventOnlyIfCounterAvailable if true, prevent only as much damage as the number of
 *                                      counters removed
 */
public record PreventDamageAndRemovePlusOnePlusOneCountersEffect(
        boolean removeOneOnly,
        CreateTokenEffect tokenTemplate,
        boolean preventOnlyIfCounterAvailable
) implements CardEffect {

    /** Default constructor: removes counters equal to damage (Protean Hydra behavior). */
    public PreventDamageAndRemovePlusOnePlusOneCountersEffect() {
        this(false, null, false);
    }

    public PreventDamageAndRemovePlusOnePlusOneCountersEffect(boolean removeOneOnly) {
        this(removeOneOnly, null, false);
    }

    /** Sekki variant: creates one token for each point of damage prevented. */
    public PreventDamageAndRemovePlusOnePlusOneCountersEffect(CreateTokenEffect tokenTemplate) {
        this(false, tokenTemplate, false);
    }

    public PreventDamageAndRemovePlusOnePlusOneCountersEffect(boolean removeOneOnly, CreateTokenEffect tokenTemplate) {
        this(removeOneOnly, tokenTemplate, false);
    }

    /** Rock Hydra variant: each available +1/+1 counter prevents one damage. */
    public static PreventDamageAndRemovePlusOnePlusOneCountersEffect onlyIfCounterAvailable() {
        return new PreventDamageAndRemovePlusOnePlusOneCountersEffect(false, null, true);
    }
}
