package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers the source planeswalker for a delayed trigger that destroys each creature that deals
 * combat damage to it until the source controller's next turn.
 */
public record RegisterDelayedDestroyCreatureDealingCombatDamageToPlaneswalkerEffect() implements CardEffect {
}
