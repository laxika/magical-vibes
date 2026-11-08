package com.github.laxika.magicalvibes.model;


import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Accumulates one source's noncombat damage during a damage instruction. Damage to several
 * recipients from the same instruction is batched together; separate instructions in an instant
 * or sorcery spell produce separate batches. The engine flushes the batches after resolution.
 */
public final class PendingSourceDamage {

    private final Card sourceCard;
    private final UUID controllerId;
    private final UUID sourcePermanentId;
    private int amount;
    private final Map<UUID, Integer> damageToPlayers = new LinkedHashMap<>();
    private final Map<UUID, Integer> damageToPermanents = new LinkedHashMap<>();
    private final Map<UUID, Permanent> damagedPermanentSnapshots = new LinkedHashMap<>();
    private final Set<UUID> damageToPermanentControllers = new LinkedHashSet<>();
    private final List<DamageRecipient> damageRecipients = new java.util.ArrayList<>();
    private final List<TriggeredAbility> selfDealsDamageAbilities;
    private UUID singleCreatureSpellTargetId;
    private final Set<UUID> instantOrSorceryDamageRecipients = new LinkedHashSet<>();
    private ResolutionDamageEvent damageEvent;

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
                               List<TriggeredAbility> selfDealsDamageAbilities) {
        this(sourceCard, controllerId, sourcePermanentId, amount, damagedPlayerId,
                damagedPermanentControllerId, null, selfDealsDamageAbilities);
    }

    public PendingSourceDamage(Card sourceCard, UUID controllerId, UUID sourcePermanentId, int amount,
                               UUID damagedPlayerId, UUID damagedPermanentControllerId,
                               UUID damagedPermanentId, List<TriggeredAbility> selfDealsDamageAbilities) {
        this(sourceCard, controllerId, sourcePermanentId, amount, damagedPlayerId,
                damagedPermanentControllerId, damagedPermanentId, selfDealsDamageAbilities, null);
    }

    public PendingSourceDamage(Card sourceCard, UUID controllerId, UUID sourcePermanentId, int amount,
                               UUID damagedPlayerId, UUID damagedPermanentControllerId,
                               UUID damagedPermanentId, List<TriggeredAbility> selfDealsDamageAbilities,
                               UUID singleCreatureSpellTargetId) {
        this.sourceCard = sourceCard;
        this.controllerId = controllerId;
        this.sourcePermanentId = sourcePermanentId;
        this.amount = amount;
        this.selfDealsDamageAbilities = selfDealsDamageAbilities == null ? null : List.copyOf(selfDealsDamageAbilities);
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

    /** Captures recipient characteristics before state-based actions can remove it. */
    public void rememberDamagedPermanent(Permanent permanent) {
        if (permanent != null) {
            damagedPermanentSnapshots.putIfAbsent(permanent.getId(), new Permanent(permanent));
        }
    }

    public Map<UUID, Permanent> getDamagedPermanentSnapshots() {
        return Map.copyOf(damagedPermanentSnapshots);
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

    /**
     * The source's "whenever this deals damage" abilities as they were when it dealt the damage (its own unless it was
     * face down or had lost them, and those granted to it), or {@code null} when none were captured: the source is
     * read when the batch is flushed.
     */
    public List<TriggeredAbility> getSelfDealsDamageAbilities() {
        return selfDealsDamageAbilities;
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

    /**
     * The resolution damage event the batch's damage was first dealt in, or {@code null} when it was dealt outside
     * one: batches of several sources with the same event are one damage event ("whenever damage is dealt to a
     * player" triggers once for it).
     */
    public ResolutionDamageEvent getDamageEvent() {
        return damageEvent;
    }

    /** Records the resolution damage event the batch's damage is dealt in, unless one is recorded already. */
    public void rememberDamageEvent(ResolutionDamageEvent event) {
        if (damageEvent == null) {
            damageEvent = event;
        }
    }

    public void recordInstantOrSorceryDamageRecipient(UUID recipientId) {
        if (recipientId != null) {
            instantOrSorceryDamageRecipients.add(recipientId);
        }
    }

    public int getInstantOrSorceryDamageRecipientCount() {
        return instantOrSorceryDamageRecipients.size();
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
                null, null, null, selfDealsDamageAbilities, singleCreatureSpellTargetId);
        damageToPlayers.forEach((playerId, playerDamage) -> copy.damageToPlayers.put(playerId, playerDamage));
        damageToPermanents.forEach((permanentId, permanentDamage) -> copy.damageToPermanents.put(permanentId, permanentDamage));
        damagedPermanentSnapshots.forEach((id, snapshot) ->
                copy.damagedPermanentSnapshots.put(id, new Permanent(snapshot)));
        copy.damageToPermanentControllers.addAll(damageToPermanentControllers);
        copy.damageRecipients.addAll(damageRecipients);
        copy.instantOrSorceryDamageRecipients.addAll(instantOrSorceryDamageRecipients);
        copy.damageEvent = damageEvent;
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
