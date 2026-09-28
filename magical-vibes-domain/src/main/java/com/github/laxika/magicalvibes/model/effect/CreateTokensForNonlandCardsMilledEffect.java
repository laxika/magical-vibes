package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;

import java.util.Objects;

/** Creates one copy of the supplied token template for each nonland card in the mill event. */
public record CreateTokensForNonlandCardsMilledEffect(CreateTokenEffect tokenTemplate)
        implements TokenCreatingEffect {

    public CreateTokensForNonlandCardsMilledEffect {
        Objects.requireNonNull(tokenTemplate, "tokenTemplate");
    }

    @Override
    public DynamicAmount tokenAmount() {
        return new EventValue();
    }

    @Override
    public CardType tokenType() {
        return tokenTemplate.tokenType();
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
