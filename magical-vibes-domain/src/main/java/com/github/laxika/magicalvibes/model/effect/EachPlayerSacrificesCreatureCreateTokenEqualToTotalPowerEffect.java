package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Each player chooses a creature to sacrifice. After all choices, the chosen creatures are
 * sacrificed together and the effect controller creates one token whose power and toughness are
 * each equal to the total effective power of the sacrificed creatures.
 */
public record EachPlayerSacrificesCreatureCreateTokenEqualToTotalPowerEffect(
        CreateTokenEffect tokenTemplate
) implements TokenCreatingEffect {

    @Override
    public DynamicAmount tokenAmount() {
        return tokenTemplate.amount();
    }

    @Override
    public CardType tokenType() {
        return tokenTemplate.tokenType();
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
