package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Death-trigger marker for returning the source card at the beginning of the next end step with
 * one fewer counter of the given type than it had immediately before dying.
 *
 * <p>The death-trigger collector snapshots the counter count and converts this marker into the
 * ordinary fixed-count delayed self-return effect. The trigger does not fire when the source had
 * no counter of the given type.
 *
 * @param counterType counter type checked at death and reduced on the return
 */
public record RegisterDelayedSelfReturnFromGraveyardWithOneFewerCounterEffect(CounterType counterType)
        implements CardEffect {
}
