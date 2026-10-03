package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Exiles a random matching card from the controller's graveyard and offers a copy for free. */
public record ExileRandomCardFromGraveyardAndMayCastCopyEffect(CardPredicate filter)
        implements CardEffect {

    public ExileRandomCardFromGraveyardAndMayCastCopyEffect() {
        this(null);
    }
}
