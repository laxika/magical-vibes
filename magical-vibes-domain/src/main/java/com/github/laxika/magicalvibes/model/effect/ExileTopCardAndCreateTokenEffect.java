package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.Objects;

/**
 * Exiles the top card of the controller's library face down, then creates the supplied token.
 * The token receives a leaves-the-battlefield trigger bound to that specific exiled card.
 */
public record ExileTopCardAndCreateTokenEffect(CreateTokenEffect token) implements TokenCreatingEffect {

    public ExileTopCardAndCreateTokenEffect {
        Objects.requireNonNull(token, "token");
    }

    @Override
    public DynamicAmount tokenAmount() {
        return token.amount();
    }

    @Override
    public CardType tokenType() {
        return token.primaryType();
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
