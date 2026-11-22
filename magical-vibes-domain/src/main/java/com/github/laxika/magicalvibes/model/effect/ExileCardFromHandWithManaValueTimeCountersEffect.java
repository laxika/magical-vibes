package com.github.laxika.magicalvibes.model.effect;

/**
 * Alaundo the Seer after its exile-a-card-from-your-hand step: the exiled card gets time counters equal to its mana
 * value and gains the cast-when-the-last-is-removed ability, then a time counter is removed from each other card its
 * controller owns in exile (also when no card was exiled).
 */
public record ExileCardFromHandWithManaValueTimeCountersEffect() implements CardEffect {
}
