package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.UUID;

/**
 * Carry-over state for Bend or Break's per-player land piles, opponent choices, and pile choices.
 * It remains queue-only while the active interaction is a multi-permanent, permanent, or may
 * choice prompt.
 */
public record PendingBendOrBreak(UUID controllerId, String sourceName,
                                 List<PlayerPiles> playerPiles, int currentPlayerIndex)
        implements PendingInteraction {

    public PendingBendOrBreak {
        playerPiles = List.copyOf(playerPiles);
    }

    public record PlayerPiles(UUID playerId, List<UUID> landIds,
                              List<UUID> pile1Ids, List<UUID> pile2Ids,
                              UUID opponentId, Boolean pile1Chosen) {

        public PlayerPiles {
            landIds = List.copyOf(landIds);
            pile1Ids = List.copyOf(pile1Ids);
            pile2Ids = List.copyOf(pile2Ids);
        }
    }
}
