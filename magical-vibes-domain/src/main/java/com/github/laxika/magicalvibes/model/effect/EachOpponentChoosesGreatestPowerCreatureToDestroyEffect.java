package com.github.laxika.magicalvibes.model.effect;

/**
 * For each opponent, the effect controller chooses one of that player's creatures tied for
 * greatest power, then destroys all chosen creatures simultaneously.
 */
public record EachOpponentChoosesGreatestPowerCreatureToDestroyEffect() implements CardEffect {
}
