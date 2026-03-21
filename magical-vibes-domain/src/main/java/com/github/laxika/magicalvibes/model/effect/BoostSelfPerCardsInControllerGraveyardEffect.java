package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Boosts this creature by +X/+Y per card matching a predicate in the
 * controller's graveyard. Used by cards like Multani, Yavimaya's Avatar
 * (gets +1/+1 for each land card in your graveyard).
 */
public record BoostSelfPerCardsInControllerGraveyardEffect(
        CardPredicate filter,
        int powerPerCard,
        int toughnessPerCard
) implements CardEffect {
}
