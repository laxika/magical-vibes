package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * The controller chooses a card type, then the target player reveals their hand and the supplied
 * token amount is created for each revealed card of that type.
 *
 * <p>The wrapped token blueprint's amount is interpreted as the number of tokens created per
 * matching card; its other characteristics are copied to every created token.</p>
 */
public record CreateTokensForEachChosenCardTypeCardInTargetHandEffect(CreateTokenEffect tokenTemplate)
        implements TokenCreatingEffect {

    public CreateTokensForEachChosenCardTypeCardInTargetHandEffect {
        if (tokenTemplate == null) {
            throw new IllegalArgumentException("tokenTemplate must not be null");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
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
