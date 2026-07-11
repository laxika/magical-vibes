package com.github.laxika.magicalvibes.model.effect;

/**
 * Death trigger for "When this creature dies, create N of the given token for each counter on it"
 * (e.g. Kinsbaile Borderguard: a 1/1 white Kithkin Soldier for each counter on it).
 * <p>
 * Placed on the {@code ON_DEATH} slot. The death-trigger collector snapshots the dying permanent's
 * total counter count (across every concrete counter type) and resolves into a copy of
 * {@code tokenTemplate} whose amount is that count, reusing the standard token-creation handler.
 *
 * @param tokenTemplate the token to create once per counter on the dying creature
 */
public record CreateTokensForEachDyingSourceCounterEffect(CreateTokenEffect tokenTemplate) implements CardEffect {
}
