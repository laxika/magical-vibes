package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Moves the cards from one exile effect that are still exiled into their owners' graveyards, then
 * creates one token for each card that actually entered a graveyard.
 */
public record PutStillExiledCardsIntoGraveyardAndCreateTokensEffect(
        List<UUID> cardIds,
        CreateTokenEffect tokenEffect
) implements CardEffect {

    public PutStillExiledCardsIntoGraveyardAndCreateTokensEffect {
        cardIds = List.copyOf(cardIds);
        if (tokenEffect == null) {
            throw new IllegalArgumentException("tokenEffect must not be null");
        }
    }
}
