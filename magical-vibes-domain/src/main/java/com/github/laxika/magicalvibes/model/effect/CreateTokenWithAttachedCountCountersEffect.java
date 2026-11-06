package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Creates a token and gives it one counter for each Aura and Equipment attached to the source, other than the permanent
 * that granted the triggered ability this effect is part of ("other than Animal Friend"), the stack entry's granting
 * permanent ({@code StackEntry.getGrantingPermanentId()}).
 */
public record CreateTokenWithAttachedCountCountersEffect(
        CreateTokenEffect tokenTemplate,
        CounterType counterType
) implements TokenCreatingEffect {

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
