package com.github.laxika.magicalvibes.model.effect;

/**
 * Queues a scry 1 effect when an earlier effect in this stack-entry resolution actually dealt
 * damage to a player.
 */
public record ScryIfPlayerDealtDamageThisWayEffect() implements CardEffect {
}
