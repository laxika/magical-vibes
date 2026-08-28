package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that turns planeswalkers with loyalty counters into creatures whose base power
 * and toughness equal their current loyalty.
 */
public record PlaneswalkersWithLoyaltyBecomeCreaturesEffect() implements CardEffect {
}
