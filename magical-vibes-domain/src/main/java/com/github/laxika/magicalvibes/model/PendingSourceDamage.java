package com.github.laxika.magicalvibes.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
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
    private final UUID sourcePermanentId;
    private int amount;
    private final Map<UUID, Integer> damageToPlayers = new LinkedHashMap<>();
    private final Set<UUID> damageToPermanentControllers = new LinkedHashSet<>();

    public PendingSourceDamage(Card sourceCard, UUID controllerId, UUID sourcePermanentId, int amount) {
        this(sourceCard, controllerId, sourcePermanentId, amount, null);
    }

    public PendingSourceDamage(Card sourceCard, UUID controllerId, UUID sourcePermanentId, int amount,
                               UUID damagedPlayerId) {
        this(sourceCard, controllerId, sourcePermanentId, amount, damagedPlayerId, null);
    }

    public PendingSourceDamage(Card sourceCard, UUID controllerId, UUID sourcePermanentId, int amount,
                               UUID damagedPlayerId, UUID damagedPermanentControllerId) {
        this.sourceCard = sourceCard;
        this.controllerId = controllerId;
        this.sourcePermanentId = sourcePermanentId;
        this.amount = amount;
        addToPlayer(damagedPlayerId, amount);
        addToPermanentController(damagedPermanentControllerId);
    }

    public Card getSourceCard() {
        return sourceCard;
    }

    public UUID getControllerId() {
        return controllerId;
    }

    public UUID getSourcePermanentId() {
        return sourcePermanentId;
    }

    public int getAmount() {
        return amount;
    }

    public Map<UUID, Integer> getDamageToPlayers() {
        return Map.copyOf(damageToPlayers);
    }

    public Set<UUID> getDamageToPermanentControllers() {
        return Set.copyOf(damageToPermanentControllers);
    }

    public void add(int extra) {
        this.amount += extra;
    }

    public void add(int extra, UUID damagedPlayerId) {
        this.amount += extra;
        addToPlayer(damagedPlayerId, extra);
    }

    public void add(int extra, UUID damagedPlayerId, UUID damagedPermanentControllerId) {
        this.amount += extra;
        addToPlayer(damagedPlayerId, extra);
        addToPermanentController(damagedPermanentControllerId);
    }

    private void addToPlayer(UUID damagedPlayerId, int amount) {
        if (damagedPlayerId != null && amount > 0) {
            damageToPlayers.merge(damagedPlayerId, amount, Integer::sum);
        }
    }

    private void addToPermanentController(UUID controllerId) {
        if (controllerId != null) {
            damageToPermanentControllers.add(controllerId);
        }
    }

    public PendingSourceDamage copy() {
        PendingSourceDamage copy = new PendingSourceDamage(sourceCard, controllerId, sourcePermanentId, amount);
        damageToPlayers.forEach((playerId, playerDamage) -> copy.damageToPlayers.put(playerId, playerDamage));
        copy.damageToPermanentControllers.addAll(damageToPermanentControllers);
        return copy;
    }
}
