package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import java.util.UUID;
import java.util.List;

/** One card in a simultaneous battlefield-entry batch, with an optional chosen Aura attachment. */
public record BattlefieldEntryCard(UUID controllerId, UUID zoneOwnerId, Card card, Zone origin,
                                   UUID attachmentId, UUID protectorPlayerId, Permanent preparedPermanent,
                                   List<StackEntry> afterEntryTriggers, boolean tapped,
                                   PutCardsOntoBattlefieldEffect.EnteringAs entering, List<UUID> hosts,
                                   boolean recordsReturn) {
    public BattlefieldEntryCard(UUID controllerId, UUID zoneOwnerId, Card card, Zone origin,
                                UUID attachmentId, UUID protectorPlayerId, Permanent preparedPermanent,
                                List<StackEntry> afterEntryTriggers, boolean tapped) {
        this(controllerId, zoneOwnerId, card, origin, attachmentId, protectorPlayerId,
                preparedPermanent, afterEntryTriggers, tapped, null, null, false);
    }

    public BattlefieldEntryCard(UUID controllerId, UUID zoneOwnerId, Card card, Zone origin,
                                UUID attachmentId, UUID protectorPlayerId, Permanent preparedPermanent,
                                List<StackEntry> afterEntryTriggers) {
        this(controllerId, zoneOwnerId, card, origin, attachmentId, protectorPlayerId,
                preparedPermanent, afterEntryTriggers, false, null, null, false);
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
                preparedPermanent, afterEntryTriggers, tapped, entering, hosts, recordsReturn);
    }

    public BattlefieldEntryCard withProtector(UUID id) {
        return new BattlefieldEntryCard(controllerId, zoneOwnerId, card, origin, attachmentId, id,
                preparedPermanent, afterEntryTriggers, tapped, entering, hosts, recordsReturn);
    }

    public BattlefieldEntryCard withPreparedPermanent(Permanent permanent, List<StackEntry> triggers) {
        return new BattlefieldEntryCard(controllerId, zoneOwnerId, card, origin, attachmentId, protectorPlayerId,
                permanent, triggers, tapped, entering, hosts, recordsReturn);
    }

    public BattlefieldEntryCard withTapped(boolean value) {
        return new BattlefieldEntryCard(controllerId, zoneOwnerId, card, origin, attachmentId, protectorPlayerId,
                preparedPermanent, afterEntryTriggers, value, entering, hosts, recordsReturn);
    }

    /** This card entering as that says, beyond what the card itself is. */
    public BattlefieldEntryCard withEntering(PutCardsOntoBattlefieldEffect.EnteringAs value) {
        return new BattlefieldEntryCard(controllerId, zoneOwnerId, card, origin, attachmentId, protectorPlayerId,
                preparedPermanent, afterEntryTriggers, tapped, value, hosts, recordsReturn);
    }

    /** This card, an Aura, entering attached to one of these permanents or players (what the effect names). */
    public BattlefieldEntryCard withHosts(List<UUID> value) {
        return new BattlefieldEntryCard(controllerId, zoneOwnerId, card, origin, attachmentId, protectorPlayerId,
                preparedPermanent, afterEntryTriggers, tapped, entering, List.copyOf(value), recordsReturn);
    }

    /** This card entering as part of a step that records what it put onto the battlefield ({@code StepKind.RETURN}). */
    public BattlefieldEntryCard recordingReturn() {
        return new BattlefieldEntryCard(controllerId, zoneOwnerId, card, origin, attachmentId, protectorPlayerId,
                preparedPermanent, afterEntryTriggers, tapped, entering, hosts, true);
    }

    public Card enteringCard() {
        return preparedPermanent == null ? card : preparedPermanent.getCard();
    }

    public BattlefieldEntryCard deepCopy() {
        return new BattlefieldEntryCard(controllerId, zoneOwnerId, card, origin, attachmentId, protectorPlayerId,
                preparedPermanent == null ? null : new Permanent(preparedPermanent),
                afterEntryTriggers.stream().map(StackEntry::new).toList(), tapped, entering, hosts, recordsReturn);
    }
}
