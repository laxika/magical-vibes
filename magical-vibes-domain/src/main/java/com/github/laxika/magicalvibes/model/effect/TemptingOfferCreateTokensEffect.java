package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.List;
import java.util.UUID;

/**
 * Creates the initial tokens for the controller, then offers each opponent the same token creation
 * in APNAP order; each accepted offer creates another copy of the tokens for the controller.
 */
public record TemptingOfferCreateTokensEffect(
        CreateTokenEffect token,
        List<UUID> remainingOpponentIds,
        UUID abilityControllerId
) implements TokenCreatingEffect {

    public TemptingOfferCreateTokensEffect(CreateTokenEffect token) {
        this(token, null, null);
    }

    public TemptingOfferCreateTokensEffect {
        if (remainingOpponentIds != null) {
            remainingOpponentIds = List.copyOf(remainingOpponentIds);
        }
    }

    @Override
    public DynamicAmount tokenAmount() {
        return token.tokenAmount();
    }

    @Override
    public CardType tokenType() {
        return token.tokenType();
    }

    @Override
    public int tokenPower() {
        return token.tokenPower();
    }

    @Override
    public int tokenToughness() {
        return token.tokenToughness();
    }
}
