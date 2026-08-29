package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player chooses from among the lands they control a land of each basic land type, then
 * returns the chosen lands to their owners' hands.
 *
 * <p>A land with multiple basic land types may be chosen for more than one type. The effect is
 * non-targeting and makes the choices in active-player order before returning any land.
 */
public record EachPlayerChoosesLandOfEachBasicTypeThenReturnToHandEffect() implements CardEffect {
}
