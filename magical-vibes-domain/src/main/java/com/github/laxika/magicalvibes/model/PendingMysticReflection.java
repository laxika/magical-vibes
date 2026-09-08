package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * One unresolved Mystic Reflection replacement, including the target's last-known card for use if
 * that permanent leaves the battlefield before the replacement applies.
 */
public record PendingMysticReflection(UUID targetPermanentId, Card lastKnownTargetCard) {
}
