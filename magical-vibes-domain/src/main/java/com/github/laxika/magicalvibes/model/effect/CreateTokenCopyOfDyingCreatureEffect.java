package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Creates a token copy of the creature that caused an ally-creature death trigger. The creature's card is the
 * stack entry's triggering card; the entry also keeps the creature's last-known card for token deaths and cards
 * removed before resolution.
 */
public record CreateTokenCopyOfDyingCreatureEffect(CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect)
        implements CardEffect, TokenCreatingEffect {

    @Override
    public DynamicAmount tokenAmount() {
        return tokenCopyEffect.amount();
    }

    @Override
    public CardType tokenType() {
        return CardType.CREATURE;
    }

    @Override
    public int tokenPower() {
        return tokenCopyEffect.powerOverride() == null ? 0 : tokenCopyEffect.powerOverride();
    }

    @Override
    public int tokenToughness() {
        return tokenCopyEffect.toughnessOverride() == null ? 0 : tokenCopyEffect.toughnessOverride();
    }
}
