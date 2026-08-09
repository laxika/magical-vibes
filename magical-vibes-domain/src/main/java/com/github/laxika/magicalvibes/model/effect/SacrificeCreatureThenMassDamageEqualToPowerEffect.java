package com.github.laxika.magicalvibes.model.effect;

/**
 * Sacrifice a creature, then deal damage equal to its power to each creature without flying and
 * each player.
 *
 * <p>The sacrifice is part of the effect, not a cost. If the controller has no creature, nothing
 * happens. The sacrificed creature's effective power is captured before it leaves the battlefield.
 */
public record SacrificeCreatureThenMassDamageEqualToPowerEffect() implements CardEffect {
}
