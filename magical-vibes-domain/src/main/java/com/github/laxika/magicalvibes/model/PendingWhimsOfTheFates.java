package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.UUID;

/**
 * Carry-over state for Whims of the Fates' sequential three-pile choices.
 * It remains queue-only while the current player is choosing Pile 1 or Pile 2.
 */
public record PendingWhimsOfTheFates(UUID controllerId, String sourceName,
                                    List<PlayerPiles> playerPiles,
                                    int currentPlayerIndex, int currentPile)
        implements PendingInteraction {

    public PendingWhimsOfTheFates {
        playerPiles = List.copyOf(playerPiles);
        if (currentPile < 1 || currentPile > 2) {
            throw new IllegalArgumentException("Whims of the Fates only prompts for piles 1 and 2");
        }
    }

    public record PlayerPiles(UUID playerId, List<UUID> permanentIds,
                              List<UUID> pile1Ids, List<UUID> pile2Ids,
                              List<UUID> pile3Ids) {

        public PlayerPiles {
            permanentIds = List.copyOf(permanentIds);
            pile1Ids = List.copyOf(pile1Ids);
            pile2Ids = List.copyOf(pile2Ids);
            pile3Ids = List.copyOf(pile3Ids);
        }
    }
}
