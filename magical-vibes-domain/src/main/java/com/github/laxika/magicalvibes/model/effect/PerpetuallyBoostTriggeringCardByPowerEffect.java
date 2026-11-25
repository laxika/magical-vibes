package com.github.laxika.magicalvibes.model.effect;

/**
 * "When you cast a creature spell, it perpetually gets +X/+X, where X is its power" (Rothga): the triggering spell's card,
 * while that spell is on the stack, gets +X/+X for its power then (CR 400.7).
 */
public record PerpetuallyBoostTriggeringCardByPowerEffect() implements CardEffect {
}
