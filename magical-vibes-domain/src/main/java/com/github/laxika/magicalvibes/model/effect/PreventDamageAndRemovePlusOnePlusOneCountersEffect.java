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
 * @param requiresCounter if true, the replacement applies only while at least one +1/+1 counter
 *                        remains on the permanent
 * @param dealsPreventedDamage if true, queue a reflexive any-target damage ability for the number
 *                             of counters removed
 * @param givesEachPlayerRadCounters if true, give each player that many rad counters immediately
 *                                   after counters are removed
 * @param preventsDamage if false, the damage is replaced by the counter removal without being
 *                       prevented, so it applies even when damage can't be prevented (e.g. Vuzzle
 *                       Spaceship, "remove that many +1/+1 counters from it instead")
 */
public record PreventDamageAndRemovePlusOnePlusOneCountersEffect(
        boolean removeOneOnly,
        CreateTokenEffect tokenTemplate,
        boolean preventOnlyIfCounterAvailable,
        boolean requiresCounter,
        boolean dealsPreventedDamage,
        boolean givesEachPlayerRadCounters,
        boolean preventsDamage
) implements CardEffect {

    /** Default constructor: removes counters equal to damage (Protean Hydra behavior). */
    public PreventDamageAndRemovePlusOnePlusOneCountersEffect() {
        this(false, null, false, false, false, false, true);
    }

    public PreventDamageAndRemovePlusOnePlusOneCountersEffect(boolean removeOneOnly) {
        this(removeOneOnly, null, false, false, false, false, true);
    }

    /** Sekki variant: creates one token for each point of damage prevented. */
    public PreventDamageAndRemovePlusOnePlusOneCountersEffect(CreateTokenEffect tokenTemplate) {
        this(false, tokenTemplate, false, false, false, false, true);
    }

    public PreventDamageAndRemovePlusOnePlusOneCountersEffect(boolean removeOneOnly, CreateTokenEffect tokenTemplate) {
        this(removeOneOnly, tokenTemplate, false, false, false, false, true);
    }

    public PreventDamageAndRemovePlusOnePlusOneCountersEffect(boolean removeOneOnly,
                                                               CreateTokenEffect tokenTemplate,
                                                               boolean preventOnlyIfCounterAvailable,
                                                               boolean requiresCounter) {
        this(removeOneOnly, tokenTemplate, preventOnlyIfCounterAvailable, requiresCounter, false, false, true);
    }

    /** Rock Hydra variant: each available +1/+1 counter prevents one damage. */
    public static PreventDamageAndRemovePlusOnePlusOneCountersEffect onlyIfCounterAvailable() {
        return new PreventDamageAndRemovePlusOnePlusOneCountersEffect(false, null, true, false, false, false, true);
    }

    /** Ugin's Conjurant variant: applies only while the permanent has a +1/+1 counter. */
    public static PreventDamageAndRemovePlusOnePlusOneCountersEffect onlyWhileCountered() {
        return new PreventDamageAndRemovePlusOnePlusOneCountersEffect(false, null, false, true, false, false, true);
    }

    /** Magma Pummeler variant: the counters removed by the replacement trigger any-target damage. */
    public static PreventDamageAndRemovePlusOnePlusOneCountersEffect withReflexiveDamageTrigger() {
        return new PreventDamageAndRemovePlusOnePlusOneCountersEffect(false, null, false, true, true, false, true);
    }

    /** Bloatfly Swarm variant: the counters removed by the replacement give each player rad counters. */
    public static PreventDamageAndRemovePlusOnePlusOneCountersEffect withEachPlayerRadCounterFollowUp() {
        return new PreventDamageAndRemovePlusOnePlusOneCountersEffect(false, null, false, true, false, true, true);
    }

    /** Vuzzle Spaceship variant: the damage is replaced by removing counters, even if it can't be prevented. */
    public static PreventDamageAndRemovePlusOnePlusOneCountersEffect replacingDamageWithCounterRemoval() {
        return new PreventDamageAndRemovePlusOnePlusOneCountersEffect(false, null, false, false, false, false, false);
    }
}
