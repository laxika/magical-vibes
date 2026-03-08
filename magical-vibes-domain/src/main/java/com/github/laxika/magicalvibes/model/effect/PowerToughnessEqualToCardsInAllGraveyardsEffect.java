package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

public record PowerToughnessEqualToCardsInAllGraveyardsEffect(CardPredicate filter) implements CardEffect {
    @Override
    public boolean isPowerToughnessDefining() { return true; }
}
