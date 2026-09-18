package com.github.laxika.magicalvibes.model;

import java.util.Set;
import java.util.UUID;

/** The result consumed by the suspended spell when its child game returns. */
public record SubgameResult(UUID gameId, Set<UUID> winners) {
    public SubgameResult {
        winners = Set.copyOf(winners);
    }
}
