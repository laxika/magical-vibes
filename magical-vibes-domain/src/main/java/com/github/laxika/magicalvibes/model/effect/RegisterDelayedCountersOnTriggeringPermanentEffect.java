package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates a delayed triggered ability when it resolves (CR 603.7a) that puts {@code counters} on the
 * permanent recorded as the stack entry's triggering permanent at the beginning of the next end step.
 * The counters are not targeted. Used by Foe-Razer Regent's "Whenever a creature you control fights,
 * put two +1/+1 counters on it at the beginning of the next end step."
 */
public record RegisterDelayedCountersOnTriggeringPermanentEffect(PutCounterOnTargetPermanentEffect counters)
        implements CardEffect {
}
