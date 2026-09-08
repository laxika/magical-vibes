package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/** Returns the remembered cards that are still exiled to the specified player's hand. */
public record ReturnExiledCardsToTargetPlayerHandEffect(UUID playerId, List<UUID> cardIds)
        implements CardEffect {

    public ReturnExiledCardsToTargetPlayerHandEffect {
        cardIds = cardIds == null ? List.of() : List.copyOf(cardIds);
    }
}
