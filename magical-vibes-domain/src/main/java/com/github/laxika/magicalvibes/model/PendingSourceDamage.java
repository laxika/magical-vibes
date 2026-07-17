package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Accumulates the damage a single source deals during one non-combat damage event (a stack-entry
 * resolution). A red source that damages several targets at once produces one summed Justice
 * reflection (per the CR ruling), so damage is batched here per source and flushed after the
 * resolution completes. Keyed in {@link GameData#pendingSourceDamageForReflection} by the source
 * card's id.
 */
public final class PendingSourceDamage {

    private final Card sourceCard;
    private final UUID controllerId;
    private int amount;

    public PendingSourceDamage(Card sourceCard, UUID controllerId, int amount) {
        this.sourceCard = sourceCard;
        this.controllerId = controllerId;
        this.amount = amount;
    }

    public Card getSourceCard() {
        return sourceCard;
    }

    public UUID getControllerId() {
        return controllerId;
    }

    public int getAmount() {
        return amount;
    }

    public void add(int extra) {
        this.amount += extra;
    }
}
