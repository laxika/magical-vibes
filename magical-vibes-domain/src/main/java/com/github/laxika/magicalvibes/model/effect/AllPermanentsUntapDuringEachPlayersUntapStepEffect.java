package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that makes every permanent untap during every player's untap step.
 *
 * <p>This is read by {@code UntapStepService}; it is not a resolving effect.
 */
public record AllPermanentsUntapDuringEachPlayersUntapStepEffect() implements CardEffect {
}
