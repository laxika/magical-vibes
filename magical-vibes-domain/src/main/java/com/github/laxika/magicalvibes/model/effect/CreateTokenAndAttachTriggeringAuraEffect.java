package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Ajani's Chosen: "create a 2/2 white Cat creature token. If that enchantment is an Aura, you may
 * attach it to the token." Placed in the {@code ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD} slot.
 *
 * <p>The token is always created; the move is offered only when the enchantment that triggered the
 * ability is an Aura that is still on the battlefield and could legally enchant the new token
 * (CR 701.3a — an Aura can't be attached to an object it couldn't enchant).
 *
 * @param token the token blueprint to create
 */
public record CreateTokenAndAttachTriggeringAuraEffect(CreateTokenEffect token)
        implements CardEffect, TokenCreatingEffect {

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
