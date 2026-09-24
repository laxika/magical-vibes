package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.UUID;

/**
 * Creates a token copy of the creature that caused an ally-creature death trigger.
 * The trigger collector binds the dying card id, while the stack entry retains the
 * creature's last-known card for token deaths and cards removed before resolution.
 */
public record CreateTokenCopyOfDyingCreatureEffect(
        UUID dyingCardId,
        CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect
) implements CardEffect, DyingCreatureCardAwareEffect, TokenCreatingEffect {

    public CreateTokenCopyOfDyingCreatureEffect(CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect) {
        this(null, tokenCopyEffect);
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new CreateTokenCopyOfDyingCreatureEffect(dyingCardId, tokenCopyEffect);
    }

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
