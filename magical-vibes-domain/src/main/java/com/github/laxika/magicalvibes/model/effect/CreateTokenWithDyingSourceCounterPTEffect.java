package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.Map;

/**
 * Creates a token whose base power and toughness equal the dying creature's +1/+1 counter count.
 * The death trigger pipeline binds the dying permanent's counter snapshot before resolution.
 */
public record CreateTokenWithDyingSourceCounterPTEffect(
        CreateTokenEffect tokenTemplate,
        Map<CounterType, Integer> counters
) implements CardEffect, DyingCreatureCountersAwareEffect, TokenCreatingEffect {

    public CreateTokenWithDyingSourceCounterPTEffect {
        counters = Map.copyOf(counters);
    }

    public CreateTokenWithDyingSourceCounterPTEffect(CreateTokenEffect tokenTemplate) {
        this(tokenTemplate, Map.of());
    }

    @Override
    public CardEffect boundToDyingCreatureCounters(Map<CounterType, Integer> counters) {
        return new CreateTokenWithDyingSourceCounterPTEffect(tokenTemplate, counters);
    }

    @Override
    public DynamicAmount tokenAmount() {
        return tokenTemplate.amount();
    }

    @Override
    public CardType tokenType() {
        return tokenTemplate.primaryType();
    }

    @Override
    public int tokenPower() {
        return tokenTemplate.tokenPower();
    }

    @Override
    public int tokenToughness() {
        return tokenTemplate.tokenToughness();
    }
}
