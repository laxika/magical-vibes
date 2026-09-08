package com.github.laxika.magicalvibes.model.effect;

/**
 * Puts a +1/+1 counter on a Dragon controlled by the resolving player for each color, then
 * records the distinct Dragons that received counters for the card's win-condition rider.
 */
public record PutCountersOnDragonOfEachColorEffect() implements CardEffect {
}
