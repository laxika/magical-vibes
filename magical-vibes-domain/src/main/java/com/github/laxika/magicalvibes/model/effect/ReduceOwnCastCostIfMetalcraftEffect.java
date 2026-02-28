package com.github.laxika.magicalvibes.model.effect;

/**
 * Reduces this spell's casting cost by the given amount if the controller has metalcraft
 * (controls three or more artifacts).
 */
public record ReduceOwnCastCostIfMetalcraftEffect(int amount) implements CardEffect {
}
