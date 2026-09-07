package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Grants a player permission to cast matching spells from any graveyard until end of turn,
 * exiling those spells instead of putting them into a graveyard after resolution.
 */
public record AllowCastMatchingCardsFromAnyGraveyardThisTurnEffect(CardPredicate filter)
        implements CardEffect {
}
