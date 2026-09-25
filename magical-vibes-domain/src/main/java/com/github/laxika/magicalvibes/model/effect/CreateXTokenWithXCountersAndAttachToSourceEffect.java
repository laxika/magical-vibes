package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.List;
import java.util.Set;

/** Creates a token with dynamic counters, then attaches the source Equipment to it. */
public record CreateXTokenWithXCountersAndAttachToSourceEffect(
        CreateXTokenWithXCountersEffect tokenEffect
) implements TokenCreatingEffect, TriggeringSpellManaValueEffect {

    public CreateXTokenWithXCountersAndAttachToSourceEffect(
            String tokenName, int power, int toughness, CardColor color, Set<CardColor> colors,
            List<CardSubtype> subtypes, CounterType counterType, DynamicAmount counterAmount) {
        this(new CreateXTokenWithXCountersEffect(
                tokenName, power, toughness, color, colors, subtypes, counterType, counterAmount));
    }

    public CreateXTokenWithXCountersAndAttachToSourceEffect(
            String tokenName, int power, int toughness, CardColor color, Set<CardColor> colors,
            List<CardSubtype> subtypes, CounterType counterType) {
        this(new CreateXTokenWithXCountersEffect(
                tokenName, power, toughness, color, colors, subtypes, counterType));
    }

    @Override
    public DynamicAmount tokenAmount() {
        return tokenEffect.tokenTemplate().amount();
    }

    @Override
    public CardType tokenType() {
        return tokenEffect.tokenTemplate().primaryType();
    }

    @Override
    public int tokenPower() {
        return tokenEffect.tokenTemplate().tokenPower();
    }

    @Override
    public int tokenToughness() {
        return tokenEffect.tokenTemplate().tokenToughness();
    }
}
