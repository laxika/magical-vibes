package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Creates a token whose base power and toughness equal the dying creature's selected counter count.
 * A null counter type counts all counter types. The dying creature is the stack entry's triggering permanent, as it
 * last existed on the battlefield (CR 603.10a).
 */
public record CreateTokenWithDyingSourceCounterPTEffect(
        CreateTokenEffect tokenTemplate,
        CounterType counterType
) implements CardEffect, TokenCreatingEffect {

    public CreateTokenWithDyingSourceCounterPTEffect(CreateTokenEffect tokenTemplate) {
        this(tokenTemplate, null);
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
