package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Creates a token, then optionally exiles cards from the controller's graveyard to repeat. */
public record CreateTokenThenMayExileCardsAndRepeatEffect(
        CreateTokenEffect tokenEffect,
        int exileCount,
        CardPredicate exileFilter
) implements CardEffect, TokenCreatingEffect {

    public CreateTokenThenMayExileCardsAndRepeatEffect(CreateTokenEffect tokenEffect, int exileCount) {
        this(tokenEffect, exileCount, null);
    }

    public CreateTokenThenMayExileCardsAndRepeatEffect {
        if (tokenEffect == null) {
            throw new IllegalArgumentException("A token effect is required");
        }
        if (exileCount <= 0) {
            throw new IllegalArgumentException("Exile count must be positive");
        }
    }

    @Override
    public DynamicAmount tokenAmount() {
        return new Fixed(1);
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
