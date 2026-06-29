package com.github.laxika.magicalvibes.model.effect;

/**
 * Death trigger for "When this creature dies, if it had one or more +1/+1 counters on it, create the
 * given token, then put this creature's counters on that token" (e.g. Ambitious Augmenter's Fractal).
 * <p>
 * Placed on the {@code ON_DEATH} slot. The death-trigger collector snapshots the dying permanent's
 * +1/+1 counter count; if it is zero the trigger does not fire (the intervening-if condition). When it
 * fires, the collector resolves into a copy of {@code tokenTemplate} carrying that many initial
 * +1/+1 counters, reusing the standard token-creation handler.
 *
 * @param tokenTemplate the token to create (its {@code initialPlusOnePlusOneCounters} is overridden
 *                      with the dying creature's counter count)
 */
public record CreateTokenWithDyingSourceCountersEffect(CreateTokenEffect tokenTemplate) implements CardEffect {
}
