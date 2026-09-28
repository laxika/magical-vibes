package com.github.laxika.magicalvibes.model.effect;

/**
 * Chooses a player at random, then queues a reflexive ability that has the source fight another
 * creature controlled by that player.
 */
public record RandomPlayerThenFightsTargetCreatureEffect() implements CardEffect {
}
