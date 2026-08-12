package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.Objects;

/**
 * Creates the supplied token blueprint and links every token created by the resolution to the
 * source permanent. The source's leaves-the-battlefield effect can then clean up those tokens,
 * while each token can refer back to the source when it leaves.
 */
public record CreateTokenAndLinkToSourceEffect(CreateTokenEffect token)
        implements TokenCreatingEffect {

    public CreateTokenAndLinkToSourceEffect {
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
