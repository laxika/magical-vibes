package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Tempting offer that pairs one card draw with one token creation for each accepting player. */
public record TemptingOfferDrawAndCreateTokenEffect(
        CreateTokenEffect tokenEffect,
        List<UUID> remainingOpponentIds,
        UUID abilityControllerId
) implements CardDrawingEffect, TokenCreatingEffect {

    public TemptingOfferDrawAndCreateTokenEffect {
        Objects.requireNonNull(tokenEffect, "tokenEffect is required");
        if (remainingOpponentIds != null) {
            remainingOpponentIds = List.copyOf(remainingOpponentIds);
        }
    }

    public TemptingOfferDrawAndCreateTokenEffect(CreateTokenEffect tokenEffect) {
        this(tokenEffect, null, null);
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(1);
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
