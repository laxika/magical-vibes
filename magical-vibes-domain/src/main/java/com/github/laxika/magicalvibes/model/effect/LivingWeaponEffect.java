package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.List;
import java.util.Set;

/**
 * When this Equipment enters the battlefield, create the configured token, then attach this
 * Equipment to the last token created.
 */
public record LivingWeaponEffect(CreateTokenEffect token) implements CardEffect, TokenCreatingEffect {

    public LivingWeaponEffect() {
        this(new CreateTokenEffect("Phyrexian Germ", 0, 0, CardColor.BLACK,
                List.of(CardSubtype.PHYREXIAN, CardSubtype.GERM), Set.of(), Set.of()));
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
