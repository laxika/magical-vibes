package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Grants the controller permission to play matching cards currently in their graveyard until
 * end of turn. The permission covers both casting spells and playing lands.
 */
public record AllowPlayMatchingCardsFromGraveyardThisTurnEffect(CardPredicate filter) implements CardEffect {
}
