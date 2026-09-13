package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Each targeted player creates the wrapped token profile under their own control. Uses
 * {@code entry.getTargetIds()} for the target list and evaluates the token profile once at
 * resolution.
 */
public record EachTargetPlayerCreatesTokensEffect(CreateTokenEffect tokenEffect)
        implements TokenCreatingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
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
