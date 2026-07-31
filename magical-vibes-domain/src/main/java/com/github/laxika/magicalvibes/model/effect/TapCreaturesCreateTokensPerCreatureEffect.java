package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * "Tap any number of untapped creatures you control. Create [token] for each creature tapped
 * this way."
 *
 * <p>On resolution the controller chooses any subset of their untapped creatures via a
 * multi-permanent choice; each chosen creature is tapped and the controller then creates one
 * {@code tokenTemplate} token per creature tapped. The template's own amount is ignored. If the
 * controller has no untapped creatures, nothing happens. Used by Devout Invocation (4/4 white
 * Angel with flying).
 *
 * <p>The token-count analogue of {@link TapCreaturesGainLifePerCreatureEffect}.
 */
public record TapCreaturesCreateTokensPerCreatureEffect(CreateTokenEffect tokenTemplate)
        implements TokenCreatingEffect {

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
