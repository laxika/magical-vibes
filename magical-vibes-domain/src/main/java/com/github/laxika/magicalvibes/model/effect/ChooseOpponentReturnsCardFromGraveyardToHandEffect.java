package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * At resolution, the controller chooses an opponent (without targeting); that player returns a card
 * matching {@code filter} (any card when {@code null}) from their graveyard to their hand.
 */
public record ChooseOpponentReturnsCardFromGraveyardToHandEffect(CardPredicate filter) implements CardEffect {
}
