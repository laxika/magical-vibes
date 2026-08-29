package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/** Marks a revealed-hand choice whose selected card must be tracked with Valki. */
public record PendingValkiHandExileChoice(UUID sourcePermanentId)
        implements PendingInteraction {
}
