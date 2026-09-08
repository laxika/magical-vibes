package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller chooses one land with each basic land type from among all battlefields, then
 * destroys the chosen lands simultaneously.
 *
 * <p>A land with multiple basic land types may be chosen for more than one type, but is destroyed
 * only once.
 */
public record ChooseLandOfEachBasicTypeThenDestroyEffect() implements CardEffect {
}
