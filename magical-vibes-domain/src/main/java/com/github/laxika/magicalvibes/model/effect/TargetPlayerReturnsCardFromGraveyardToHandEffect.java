package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** The targeted player chooses a matching card from their graveyard and returns it to their hand. */
public record TargetPlayerReturnsCardFromGraveyardToHandEffect(CardPredicate filter) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
