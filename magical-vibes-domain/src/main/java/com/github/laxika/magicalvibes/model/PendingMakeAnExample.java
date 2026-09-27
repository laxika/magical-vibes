package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.UUID;

/** Progress state for Make an Example's per-opponent creature pile choices. */
public record PendingMakeAnExample(UUID controllerId, List<UUID> opponentIds,
                                   int currentOpponentIndex, String sourceName)
        implements PendingInteraction {

    public PendingMakeAnExample {
        opponentIds = List.copyOf(opponentIds);
    }

    @Override
    public UUID decidingPlayerId() {
        return controllerId;
    }

    @Override
    public InteractionOptions legalOptions() {
        return InteractionOptions.UNENUMERATED;
    }
}
