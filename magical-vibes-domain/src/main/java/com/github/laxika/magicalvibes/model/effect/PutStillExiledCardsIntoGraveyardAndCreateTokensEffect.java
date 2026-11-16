package com.github.laxika.magicalvibes.model.effect;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Moves the cards from one exile effect that are still exiled into their owners' graveyards, then
 * creates one token for each card that actually entered a graveyard.
 *
 * @param exileEntryVersions each card's exile entry version as the exile effect left it, in order: a card that left
 *                           exile and was exiled again is a new object (CR 400.7) and stays
 */
public record PutStillExiledCardsIntoGraveyardAndCreateTokensEffect(
        Map<UUID, Long> exileEntryVersions,
        CreateTokenEffect tokenEffect
) implements CardEffect {

    public PutStillExiledCardsIntoGraveyardAndCreateTokensEffect {
        exileEntryVersions = java.util.Collections.unmodifiableMap(new LinkedHashMap<>(exileEntryVersions));
        if (tokenEffect == null) {
            throw new IllegalArgumentException("tokenEffect must not be null");
        }
    }
}
