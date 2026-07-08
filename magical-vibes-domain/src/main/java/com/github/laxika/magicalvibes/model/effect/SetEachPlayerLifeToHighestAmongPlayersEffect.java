package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player's life total becomes the highest life total among all players.
 * Used by Arbiter of Knollridge.
 */
public record SetEachPlayerLifeToHighestAmongPlayersEffect() implements CardEffect {
}
