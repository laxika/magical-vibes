package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Puts each of the named cards that is still in exile into its owner's graveyard. Created by delayed
 * "return them to their owners' graveyards" triggers that remember their cards.
 */
public record PutExiledCardsIntoOwnersGraveyardEffect(List<UUID> exiledCardIds) implements CardEffect {

    public PutExiledCardsIntoOwnersGraveyardEffect {
        exiledCardIds = List.copyOf(exiledCardIds);
    }
}
