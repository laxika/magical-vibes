package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** A matching attacker captured when an attack trigger is created. */
public record AttackingPermanentSnapshot(UUID permanentId, int powerAtTrigger) {
}
