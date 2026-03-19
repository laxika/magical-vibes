package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: each spell a player casts costs {@code amountPerSpell} more generic mana
 * for each other spell that player has already cast this turn. Affects all players.
 * Used by Damping Sphere.
 */
public record IncreaseEachPlayerCastCostPerSpellThisTurnEffect(int amountPerSpell) implements CardEffect {
}
