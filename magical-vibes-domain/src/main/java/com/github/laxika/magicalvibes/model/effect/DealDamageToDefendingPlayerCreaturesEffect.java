package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals a fixed amount of damage to each creature controlled by the player being attacked.
 * The attacked player or planeswalker is preserved as non-targeting combat context on the stack
 * entry, so no player target is chosen.
 */
public record DealDamageToDefendingPlayerCreaturesEffect(int damage) implements CardEffect {
}
