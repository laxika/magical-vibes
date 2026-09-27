package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.Objects;

/** Mills each player, then creates one token for each creature card milled into a graveyard. */
public record MillEachPlayerAndCreateTokensForMilledCreaturesEffect(
        DynamicAmount count,
        CreateTokenEffect tokenTemplate
) implements TokenCreatingEffect {

    public MillEachPlayerAndCreateTokensForMilledCreaturesEffect {
        Objects.requireNonNull(count, "count");
        Objects.requireNonNull(tokenTemplate, "tokenTemplate");
    }

    public MillEachPlayerAndCreateTokensForMilledCreaturesEffect(int count,
                                                                  CreateTokenEffect tokenTemplate) {
        this(new Fixed(count), tokenTemplate);
    }

    @Override
    public DynamicAmount tokenAmount() {
        return new EventValue();
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
