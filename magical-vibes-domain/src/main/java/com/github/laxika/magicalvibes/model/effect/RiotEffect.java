package com.github.laxika.magicalvibes.model.effect;

/**
 * Internal pending-choice marker for riot. Accepting the choice adds a +1/+1 counter; declining
 * it gives the entering permanent haste.
 */
public record RiotEffect() implements CardEffect {
}
