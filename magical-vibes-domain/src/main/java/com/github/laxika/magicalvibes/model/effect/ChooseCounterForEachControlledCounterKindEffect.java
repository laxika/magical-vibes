package com.github.laxika.magicalvibes.model.effect;

/**
 * For each distinct counter kind on a permanent controlled by the source controller, optionally
 * puts either a +1/+1 counter or a counter of that kind on the source permanent.
 */
public record ChooseCounterForEachControlledCounterKindEffect() implements CardEffect {
}
