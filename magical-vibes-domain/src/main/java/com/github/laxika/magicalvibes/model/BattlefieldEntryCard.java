package com.github.laxika.magicalvibes.model;

import java.util.UUID;
import java.util.List;

/** One card in a simultaneous battlefield-entry batch, with an optional chosen Aura attachment. */
public record BattlefieldEntryCard(UUID controllerId, UUID zoneOwnerId, Card card, Zone origin,
                                   UUID attachmentId, UUID protectorPlayerId, Permanent preparedPermanent,
                                   List<StackEntry> afterEntryTriggers, boolean tapped) {
    public BattlefieldEntryCard(UUID controllerId, UUID zoneOwnerId, Card card, Zone origin,
                                UUID attachmentId, UUID protectorPlayerId, Permanent preparedPermanent,
                                List<StackEntry> afterEntryTriggers) {
        this(controllerId, zoneOwnerId, card, origin, attachmentId, protectorPlayerId,
                preparedPermanent, afterEntryTriggers, false);
    }
    public BattlefieldEntryCard(UUID controllerId, UUID zoneOwnerId, Card card, Zone origin,
                                UUID attachmentId, UUID protectorPlayerId) {
        this(controllerId, zoneOwnerId, card, origin, attachmentId, protectorPlayerId, null, List.of());
    }

    public BattlefieldEntryCard {
        afterEntryTriggers = List.copyOf(afterEntryTriggers);
    }
    public BattlefieldEntryCard(UUID controllerId, UUID zoneOwnerId, Card card, Zone origin,
                                UUID attachmentId) {
        this(controllerId, zoneOwnerId, card, origin, attachmentId, null);
    }

    public BattlefieldEntryCard withAttachment(UUID id) {
        return new BattlefieldEntryCard(controllerId, zoneOwnerId, card, origin, id, protectorPlayerId,
                preparedPermanent, afterEntryTriggers, tapped);
    }

    public BattlefieldEntryCard withProtector(UUID id) {
        return new BattlefieldEntryCard(controllerId, zoneOwnerId, card, origin, attachmentId, id,
                preparedPermanent, afterEntryTriggers, tapped);
    }

    public BattlefieldEntryCard withPreparedPermanent(Permanent permanent, List<StackEntry> triggers) {
        return new BattlefieldEntryCard(controllerId, zoneOwnerId, card, origin, attachmentId, protectorPlayerId,
                permanent, triggers, tapped);
    }

    public BattlefieldEntryCard withTapped(boolean value) {
        return new BattlefieldEntryCard(controllerId, zoneOwnerId, card, origin, attachmentId, protectorPlayerId,
                preparedPermanent, afterEntryTriggers, value);
    }

    public Card enteringCard() {
        return preparedPermanent == null ? card : preparedPermanent.getCard();
    }

    public BattlefieldEntryCard deepCopy() {
        return new BattlefieldEntryCard(controllerId, zoneOwnerId, card, origin, attachmentId, protectorPlayerId,
                preparedPermanent == null ? null : new Permanent(preparedPermanent),
                afterEntryTriggers.stream().map(StackEntry::new).toList(), tapped);
    }
}
