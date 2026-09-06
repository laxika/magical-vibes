package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
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
    private final Map<UUID, Integer> damageToPermanents = new LinkedHashMap<>();
    private final Set<UUID> damageToPermanentControllers = new LinkedHashSet<>();
    private final List<DamageRecipient> damageRecipients = new java.util.ArrayList<>();
    private final List<CardEffect> selfDealsDamageEffects;
    private UUID singleCreatureSpellTargetId;

    public PendingSourceDamage(Card sourceCard, UUID controllerId, UUID sourcePermanentId, int amount) {
        this(sourceCard, controllerId, sourcePermanentId, amount, null);
    }

    public PendingSourceDamage(Card sourceCard, UUID controllerId, UUID sourcePermanentId, int amount,
                               UUID damagedPlayerId) {
        this(sourceCard, controllerId, sourcePermanentId, amount, damagedPlayerId, null);
    }

    public PendingSourceDamage(Card sourceCard, UUID controllerId, UUID sourcePermanentId, int amount,
                               UUID damagedPlayerId, UUID damagedPermanentControllerId) {
        this(sourceCard, controllerId, sourcePermanentId, amount, damagedPlayerId,
                damagedPermanentControllerId, null);
    }

    public PendingSourceDamage(Card sourceCard, UUID controllerId, UUID sourcePermanentId, int amount,
                               UUID damagedPlayerId, UUID damagedPermanentControllerId,
                               List<CardEffect> selfDealsDamageEffects) {
        this(sourceCard, controllerId, sourcePermanentId, amount, damagedPlayerId,
                damagedPermanentControllerId, null, selfDealsDamageEffects);
    }

    public PendingSourceDamage(Card sourceCard, UUID controllerId, UUID sourcePermanentId, int amount,
                               UUID damagedPlayerId, UUID damagedPermanentControllerId,
                               UUID damagedPermanentId, List<CardEffect> selfDealsDamageEffects) {
        this(sourceCard, controllerId, sourcePermanentId, amount, damagedPlayerId,
                damagedPermanentControllerId, damagedPermanentId, selfDealsDamageEffects, null);
    }

    public PendingSourceDamage(Card sourceCard, UUID controllerId, UUID sourcePermanentId, int amount,
                               UUID damagedPlayerId, UUID damagedPermanentControllerId,
                               UUID damagedPermanentId, List<CardEffect> selfDealsDamageEffects,
                               UUID singleCreatureSpellTargetId) {
        this.sourceCard = sourceCard;
        this.controllerId = controllerId;
        this.sourcePermanentId = sourcePermanentId;
        this.amount = amount;
        this.selfDealsDamageEffects = selfDealsDamageEffects == null ? null : List.copyOf(selfDealsDamageEffects);
        this.singleCreatureSpellTargetId = singleCreatureSpellTargetId;
        addToPlayer(damagedPlayerId, amount);
        addToPermanent(damagedPermanentId, amount);
        addToPermanentController(damagedPermanentControllerId);
        addDamageRecipient(damagedPlayerId, damagedPermanentControllerId, damagedPermanentId);
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

    public Map<UUID, Integer> getDamageToPermanents() {
        return Map.copyOf(damageToPermanents);
    }

    public UUID getSingleCreatureSpellTargetId() {
        return singleCreatureSpellTargetId;
    }

    public Set<UUID> getDamageToPermanentControllers() {
        return Set.copyOf(damageToPermanentControllers);
    }

    public List<DamageRecipient> getDamageRecipients() {
        return List.copyOf(damageRecipients);
    }

    public List<CardEffect> getSelfDealsDamageEffects() {
        return selfDealsDamageEffects;
    }

    public void add(int extra) {
        this.amount += extra;
    }

    public void add(int extra, UUID damagedPlayerId) {
        this.amount += extra;
        addToPlayer(damagedPlayerId, extra);
        addDamageRecipient(damagedPlayerId, null, null);
    }

    public void add(int extra, UUID damagedPlayerId, UUID damagedPermanentControllerId) {
        add(extra, damagedPlayerId, damagedPermanentControllerId, null);
    }

    public void add(int extra, UUID damagedPlayerId, UUID damagedPermanentControllerId,
                    UUID damagedPermanentId) {
        this.amount += extra;
        addToPlayer(damagedPlayerId, extra);
        addToPermanent(damagedPermanentId, extra);
        addToPermanentController(damagedPermanentControllerId);
        addDamageRecipient(damagedPlayerId, damagedPermanentControllerId, damagedPermanentId);
    }

    private void addToPlayer(UUID damagedPlayerId, int amount) {
        if (damagedPlayerId != null && amount > 0) {
            damageToPlayers.merge(damagedPlayerId, amount, Integer::sum);
        }
    }

    private void addToPermanent(UUID damagedPermanentId, int amount) {
        if (damagedPermanentId != null && amount > 0) {
            damageToPermanents.merge(damagedPermanentId, amount, Integer::sum);
        }
    }

    public void rememberSingleCreatureSpellTarget(UUID targetId) {
        if (singleCreatureSpellTargetId == null) {
            singleCreatureSpellTargetId = targetId;
        }
    }

    private void addToPermanentController(UUID controllerId) {
        if (controllerId != null) {
            damageToPermanentControllers.add(controllerId);
        }
    }

    public PendingSourceDamage copy() {
        PendingSourceDamage copy = new PendingSourceDamage(sourceCard, controllerId, sourcePermanentId, amount,
                null, null, null, selfDealsDamageEffects, singleCreatureSpellTargetId);
        damageToPlayers.forEach((playerId, playerDamage) -> copy.damageToPlayers.put(playerId, playerDamage));
        damageToPermanents.forEach((permanentId, permanentDamage) -> copy.damageToPermanents.put(permanentId, permanentDamage));
        copy.damageToPermanentControllers.addAll(damageToPermanentControllers);
        copy.damageRecipients.addAll(damageRecipients);
        return copy;
    }

    private void addDamageRecipient(UUID damagedPlayerId, UUID damagedPermanentControllerId,
                                    UUID damagedPermanentId) {
        if (damagedPlayerId != null) {
            damageRecipients.add(new DamageRecipient(damagedPlayerId, null));
        } else if (damagedPermanentControllerId != null) {
            damageRecipients.add(new DamageRecipient(damagedPermanentControllerId, damagedPermanentId));
        }
    }

    public record DamageRecipient(UUID playerId, UUID permanentId) {
    }
}
