package com.github.laxika.magicalvibes.model.effect;

/**
 * Death trigger that creates one token whose power and toughness equal the total number of
 * counters on the dying source.
 */
public record CreateTokenWithDyingSourceCounterPTEffect(CreateTokenEffect tokenTemplate)
        implements CardEffect {
}
