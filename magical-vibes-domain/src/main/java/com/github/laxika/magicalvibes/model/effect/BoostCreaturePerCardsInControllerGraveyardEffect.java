package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Boosts the creature identified by {@code scope} by +X/+Y per card in the
 * controller's graveyard matching the given predicate.
 * Used by cards like Runechanter's Pike (instants and sorceries, power only).
 */
public record BoostCreaturePerCardsInControllerGraveyardEffect(
        CardPredicate filter,
        int powerPerCard,
        int toughnessPerCard,
        GrantScope scope
) implements CardEffect {
}
