package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/** One card in a simultaneous battlefield-entry batch, with an optional chosen Aura attachment. */
public record BattlefieldEntryCard(UUID controllerId, UUID zoneOwnerId, Card card, Zone origin,
                                   UUID attachmentId) {
    public BattlefieldEntryCard withAttachment(UUID id) {
        return new BattlefieldEntryCard(controllerId, zoneOwnerId, card, origin, id);
    }
}
