package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Exiles groups of cards from the controller's library, repeating while the last card in a full
 * group is a nonland card, then creates one token for each nonland card exiled this way.
 */
public record ExileTopCardsRepeatIfLastNonlandCreateTokensEffect(
        int cardsPerIteration,
        CreateTokenEffect tokenTemplate
) implements TokenCreatingEffect {

    public ExileTopCardsRepeatIfLastNonlandCreateTokensEffect {
        if (cardsPerIteration <= 0) {
            throw new IllegalArgumentException("cardsPerIteration must be positive");
        }
        if (tokenTemplate == null) {
            throw new IllegalArgumentException("tokenTemplate must not be null");
        }
    }

    @Override
    public DynamicAmount tokenAmount() {
        return tokenTemplate.amount();
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
