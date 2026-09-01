package com.github.laxika.magicalvibes.model.effect;

/** Gives the source +1/+1 until end of turn, or puts a +1/+1 counter on it instead when exactly
 * three colors of mana were spent to activate the ability. */
public record BoostSelfOrPutCounterIfExactlyThreeColorsEffect() implements CardEffect {
}
