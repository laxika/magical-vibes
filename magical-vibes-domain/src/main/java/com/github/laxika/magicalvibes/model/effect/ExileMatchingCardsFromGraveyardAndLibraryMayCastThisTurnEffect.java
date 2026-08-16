package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exiles any number of matching cards chosen from the controller's graveyard and library, then
 * grants permission to cast each exiled card until end of turn.
 */
public record ExileMatchingCardsFromGraveyardAndLibraryMayCastThisTurnEffect(CardPredicate filter)
        implements CardEffect {
}
