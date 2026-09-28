package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player chooses a creature, puts a vow counter on it, and sacrifices their other creatures.
 * The chosen creatures cannot attack the resolving spell's controller or that player's
 * planeswalkers while they have a vow counter.
 */
public record PromiseOfLoyaltyEffect() implements CardEffect {
}
