package com.github.laxika.magicalvibes.model.effect;

/**
 * Soulbond's "another creature you control enters" half (CR 702.95a): pair this creature with "that creature", the
 * stack entry's triggering permanent, read as the ability resolves. The card writes it in a {@link MayEffect} behind
 * the intervening "if" that both creatures are unpaired and under its controller's control.
 */
public record SoulbondPairWithEnteringEffect() implements CardEffect {
}
