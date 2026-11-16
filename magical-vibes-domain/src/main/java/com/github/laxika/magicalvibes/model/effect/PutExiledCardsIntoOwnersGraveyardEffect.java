package com.github.laxika.magicalvibes.model.effect;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Puts each of the named cards that is still in exile into its owner's graveyard. Created by delayed
 * "return them to their owners' graveyards" triggers that remember their cards.
 *
 * @param exileEntryVersions each card's exile entry version as the delayed trigger was made, in order: a card that
 *                           left exile and was exiled again is a new object (CR 400.7) and stays
 */
public record PutExiledCardsIntoOwnersGraveyardEffect(Map<UUID, Long> exileEntryVersions) implements CardEffect {

    public PutExiledCardsIntoOwnersGraveyardEffect {
        exileEntryVersions = java.util.Collections.unmodifiableMap(new LinkedHashMap<>(exileEntryVersions));
    }
}
