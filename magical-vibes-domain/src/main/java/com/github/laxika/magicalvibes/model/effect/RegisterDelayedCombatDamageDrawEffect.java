package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a rest-of-turn trigger that draws a card whenever a creature the controller controls
 * deals combat damage to a player or planeswalker.
 */
public record RegisterDelayedCombatDamageDrawEffect() implements CardEffect {
}
