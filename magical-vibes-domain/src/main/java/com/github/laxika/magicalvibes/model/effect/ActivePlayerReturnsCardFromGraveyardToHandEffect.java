package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The active player may choose a card matching {@code filter} in their graveyard and return it
 * to their hand. The player target is the upkeep trigger's separate target restriction; the card
 * itself is chosen when this effect resolves.
 *
 * @param filter the graveyard card filter
 */
public record ActivePlayerReturnsCardFromGraveyardToHandEffect(CardPredicate filter) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
