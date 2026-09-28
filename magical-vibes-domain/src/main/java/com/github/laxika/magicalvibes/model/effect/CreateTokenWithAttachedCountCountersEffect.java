package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import java.util.UUID;

/** Creates a token and gives it one counter for each attached Aura or Equipment, excluding one attachment. */
public record CreateTokenWithAttachedCountCountersEffect(
        CreateTokenEffect tokenTemplate,
        CounterType counterType,
        UUID excludedAttachedPermanentId
) implements TokenCreatingEffect, GrantingPermanentAwareEffect {

    public CreateTokenWithAttachedCountCountersEffect(CreateTokenEffect tokenTemplate, CounterType counterType) {
        this(tokenTemplate, counterType, null);
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

    @Override
    public CardEffect withGrantingPermanentId(UUID permanentId) {
        return new CreateTokenWithAttachedCountCountersEffect(tokenTemplate, counterType, permanentId);
    }
}
