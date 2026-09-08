package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys the creatures that blocked or were blocked by the dying source.
 *
 * <p>The death-trigger collector snapshots those combat opponents into the stack entry because
 * the source is already off the battlefield when the ability resolves.</p>
 */
public record DestroyCombatOpponentsOnDeathEffect() implements CardEffect {
}
