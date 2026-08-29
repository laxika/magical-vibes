package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exiles an exact number of targeted cards from a targeted player's graveyard and applies life
 * changes based on the cards actually exiled at resolution.
 */
public record ExileTargetPlayerGraveyardCardsEffect(
        CardPredicate filter,
        int lifeLossPerExiledCard,
        int lifeGainPerExiledCard
) implements TargetPlayerGraveyardExileEffect {

    public ExileTargetPlayerGraveyardCardsEffect(int lifeLossPerExiledCard, int lifeGainPerExiledCard) {
        this(null, lifeLossPerExiledCard, lifeGainPerExiledCard);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }

    @Override
    public boolean resolvesWhenTargetIllegal() {
        return true;
    }
}
