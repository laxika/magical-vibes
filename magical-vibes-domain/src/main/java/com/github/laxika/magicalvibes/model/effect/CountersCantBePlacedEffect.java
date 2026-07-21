package com.github.laxika.magicalvibes.model.effect;

/**
 * Global static (Solemnity): "Players can't get counters. Counters can't be put on artifacts,
 * creatures, enchantments, or lands." A single replacement lock queried at the two counter
 * chokepoints — {@code GameQueryService.cantHaveCounters} (permanent clause, restricted to
 * artifact/creature/enchantment/land permanents) and {@code canPlayerGetPoisonCounters} (player
 * clause). Loyalty counters on planeswalkers that aren't also one of those types are unaffected,
 * matching the printed text which omits planeswalkers.
 */
public record CountersCantBePlacedEffect() implements CardEffect {
}
