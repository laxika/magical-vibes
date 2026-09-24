package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Tempting offer that creates tokens for the spell's controller, then offers each opponent the
 * same token creation; an opponent who accepts also gives the spell's controller another batch.
 */
public record TemptingOfferCreateTokensEffect(
        CreateTokenEffect tokenEffect,
        List<UUID> remainingOpponentIds,
        UUID abilityControllerId
) implements TokenCreatingEffect {

    public TemptingOfferCreateTokensEffect {
        Objects.requireNonNull(tokenEffect, "tokenEffect is required");
        if (remainingOpponentIds != null) {
            remainingOpponentIds = List.copyOf(remainingOpponentIds);
        }
    }

    public TemptingOfferCreateTokensEffect(CreateTokenEffect tokenEffect) {
        this(tokenEffect, null, null);
    }

    @Override
    public DynamicAmount tokenAmount() {
        return tokenEffect.amount();
    }

    @Override
    public CardType tokenType() {
        return tokenEffect.primaryType();
    }

    @Override
    public int tokenPower() {
        return tokenEffect.tokenPower();
    }

    @Override
    public int tokenToughness() {
        return tokenEffect.tokenToughness();
    }
}
