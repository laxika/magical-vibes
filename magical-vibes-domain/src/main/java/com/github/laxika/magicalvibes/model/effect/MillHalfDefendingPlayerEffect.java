package com.github.laxika.magicalvibes.model.effect;

/**
 * "Whenever this creature attacks, defending player mills half their library, rounded up."
 *
 * <p>Placed on {@code ON_ATTACK}. Non-targeting: the player being attacked is captured as the
 * trigger's {@code attackedTargetId}, so the attacker's controller is never asked to choose a
 * player.
 */
public record MillHalfDefendingPlayerEffect() implements CardEffect {
}
