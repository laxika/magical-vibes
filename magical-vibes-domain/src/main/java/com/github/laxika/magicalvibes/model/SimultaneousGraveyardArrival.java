package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Cards one event put into {@code ownerId}'s graveyard at the same time, in the order the engine put them there: the
 * owner may arrange them (CR 404.3).
 */
public record SimultaneousGraveyardArrival(UUID ownerId, List<UUID> cardIds) {

    public SimultaneousGraveyardArrival {
        Objects.requireNonNull(ownerId, "ownerId");
        cardIds = List.copyOf(cardIds);
    }
}
