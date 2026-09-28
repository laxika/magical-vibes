package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * Creates one batch of the wrapped token for each creature controlled by the selected target
 * players, under the effect controller's control.
 */
public record CreateTokensForEachTargetPlayerCreatureEffect(CreateTokenEffect tokenTemplate)
        implements TokenCreatingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }

    @Override
    public com.github.laxika.magicalvibes.model.amount.DynamicAmount tokenAmount() {
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
