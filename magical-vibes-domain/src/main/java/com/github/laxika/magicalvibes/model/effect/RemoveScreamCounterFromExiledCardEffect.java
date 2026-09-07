package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Removes one scream counter from a card in exile and optionally resolves an effect after its last counter is removed. */
public record RemoveScreamCounterFromExiledCardEffect(UUID cardId, CardEffect whenLastCounterRemoved)
        implements CardEffect {

    public RemoveScreamCounterFromExiledCardEffect(UUID cardId) {
        this(cardId, null);
    }
}
