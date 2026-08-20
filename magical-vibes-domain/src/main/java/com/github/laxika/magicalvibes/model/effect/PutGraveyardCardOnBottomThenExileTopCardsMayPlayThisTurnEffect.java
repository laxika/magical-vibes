package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Offers an optional choice to put one matching card from the controller's graveyard on the bottom
 * of their library, then exiles cards from the top of that library and grants permission to play
 * them this turn.
 */
public record PutGraveyardCardOnBottomThenExileTopCardsMayPlayThisTurnEffect(
        CardPredicate filter, int exileCount) implements CardEffect {
}
