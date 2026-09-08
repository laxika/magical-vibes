package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Each player may shuffle their hand and graveyard into their library, then draws a fixed number
 * of cards if they chose to do so.
 *
 * <p>The player choices are carried in APNAP order so all choices are made before any accepted
 * player's zones are changed.
 */
public record EachPlayerMayShuffleZonesIntoLibraryAndDrawEffect(
        int cardsToDraw,
        List<UUID> remainingPlayerIds,
        List<UUID> acceptedPlayerIds
) implements CardEffect {

    public EachPlayerMayShuffleZonesIntoLibraryAndDrawEffect(int cardsToDraw) {
        this(cardsToDraw, List.of(), List.of());
    }

    public EachPlayerMayShuffleZonesIntoLibraryAndDrawEffect {
        remainingPlayerIds = List.copyOf(remainingPlayerIds);
        acceptedPlayerIds = List.copyOf(acceptedPlayerIds);
    }
}
