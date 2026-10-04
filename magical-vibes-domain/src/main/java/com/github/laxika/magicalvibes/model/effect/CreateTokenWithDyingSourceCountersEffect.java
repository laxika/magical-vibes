package com.github.laxika.magicalvibes.model.effect;

/**
 * Death trigger for "When this creature dies, if it had one or more counters on it, create the
 * given token, then put this creature's counters on that token" (e.g. Ambitious Augmenter's Fractal).
 * <p>
 * Placed on the {@code ON_DEATH} slot. The death-trigger collector snapshots the dying permanent's
 * counters of every type; if there are none the trigger does not fire. The token enters first,
 * then the saved counters are placed on it using the standard counter-placement effects.
 *
 * @param tokenTemplate the token to create before placing the dying creature's counters
 */
public record CreateTokenWithDyingSourceCountersEffect(CreateTokenEffect tokenTemplate) implements CardEffect {
}
