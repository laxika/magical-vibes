package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/** Carries one of Memories Returning's alternating revealed-card choices. */
public record PendingMemoriesReturningChoice(UUID controllerId, UUID opponentId,
                                             int phase, String sourceCardName)
        implements PendingInteraction {
}
