package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/** Damage Comeuppance deals to the source creature or its controller after prevention. */
public record PendingComeuppanceDamage(
        UUID targetId,
        int amount,
        UUID controllerId,
        Card sourceCard
) {
}
