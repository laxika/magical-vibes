package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/** Routes a library-reveal answer to Valki's copy choice. */
public record PendingValkiCopyChoice(UUID sourcePermanentId, int manaValue)
        implements PendingInteraction {
}
