package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, registers a delayed triggered ability for the rest of the turn: whenever a
 * creature you control attacks and isn't blocked, you may have it deal damage equal to its power to
 * a target creature. If you do, it assigns no combat damage this turn.
 *
 * <p>Registered by Gaze of Pain. See {@code DelayedUnblockedAttackerPowerDamage}.
 */
public record RegisterDelayedUnblockedAttackerPowerDamageEffect() implements CardEffect {
}
