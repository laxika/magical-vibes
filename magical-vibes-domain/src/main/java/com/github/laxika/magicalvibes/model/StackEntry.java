package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class StackEntry {

    private final StackEntryType entryType;
    private final Card card;
    private final UUID controllerId;
    private final String description;
    private final List<CardEffect> effectsToResolve;
    private final int xValue;
    @Setter private UUID targetPermanentId;
    private final UUID sourcePermanentId;
    private final Map<UUID, Integer> damageAssignments;
    private final Zone targetZone;
    private final List<UUID> targetCardIds;
    @Setter private TargetFilter targetFilter;
    @Setter private boolean copy;
    @Setter private boolean nonTargeting;
    @Setter private boolean returnToHandAfterResolving;
    @Setter private boolean castWithFlashback;
    @Setter private Card damageSourceCard;
    private final List<UUID> targetPermanentIds;

    // Creature spell constructor
    public StackEntry(Card card, UUID controllerId) {
        this.entryType = StackEntryType.CREATURE_SPELL;
        this.card = card;
        this.controllerId = controllerId;
        this.description = card.getName();
        this.effectsToResolve = List.of();
        this.xValue = 0;
        this.targetPermanentId = null;
        this.sourcePermanentId = null;
        this.damageAssignments = Map.of();
        this.targetZone = null;
        this.targetCardIds = List.of();
        this.targetFilter = null;
        this.targetPermanentIds = List.of();
    }

    // Triggered ability constructor
    public StackEntry(StackEntryType entryType, Card card, UUID controllerId, String description, List<CardEffect> effectsToResolve) {
        this.entryType = entryType;
        this.card = card;
        this.controllerId = controllerId;
        this.description = description;
        this.effectsToResolve = effectsToResolve;
        this.xValue = 0;
        this.targetPermanentId = null;
        this.sourcePermanentId = null;
        this.damageAssignments = Map.of();
        this.targetZone = null;
        this.targetCardIds = List.of();
        this.targetFilter = null;
        this.targetPermanentIds = List.of();
    }

    // General constructor with xValue (for sorcery spells)
    public StackEntry(StackEntryType entryType, Card card, UUID controllerId, String description, List<CardEffect> effectsToResolve, int xValue) {
        this.entryType = entryType;
        this.card = card;
        this.controllerId = controllerId;
        this.description = description;
        this.effectsToResolve = effectsToResolve;
        this.xValue = xValue;
        this.targetPermanentId = null;
        this.sourcePermanentId = null;
        this.damageAssignments = Map.of();
        this.targetZone = null;
        this.targetCardIds = List.of();
        this.targetFilter = null;
        this.targetPermanentIds = List.of();
    }

    // Targeted or damage distribution spell constructor
    public StackEntry(StackEntryType entryType, Card card, UUID controllerId, String description, List<CardEffect> effectsToResolve, int xValue, UUID targetPermanentId, Map<UUID, Integer> damageAssignments) {
        this.entryType = entryType;
        this.card = card;
        this.controllerId = controllerId;
        this.description = description;
        this.effectsToResolve = effectsToResolve;
        this.xValue = xValue;
        this.targetPermanentId = targetPermanentId;
        this.sourcePermanentId = null;
        this.damageAssignments = damageAssignments != null ? damageAssignments : Map.of();
        this.targetZone = null;
        this.targetCardIds = List.of();
        this.targetFilter = null;
        this.targetPermanentIds = List.of();
    }

    // Triggered ability with source and target permanent constructor
    public StackEntry(StackEntryType entryType, Card card, UUID controllerId, String description, List<CardEffect> effectsToResolve, UUID targetPermanentId, UUID sourcePermanentId) {
        this.entryType = entryType;
        this.card = card;
        this.controllerId = controllerId;
        this.description = description;
        this.effectsToResolve = effectsToResolve;
        this.xValue = 0;
        this.targetPermanentId = targetPermanentId;
        this.sourcePermanentId = sourcePermanentId;
        this.damageAssignments = Map.of();
        this.targetZone = null;
        this.targetCardIds = List.of();
        this.targetFilter = null;
        this.targetPermanentIds = List.of();
    }

    // Zone-aware targeted ability constructor (e.g. target a card in graveyard)
    public StackEntry(StackEntryType entryType, Card card, UUID controllerId, String description, List<CardEffect> effectsToResolve, UUID targetPermanentId, Zone targetZone) {
        this.entryType = entryType;
        this.card = card;
        this.controllerId = controllerId;
        this.description = description;
        this.effectsToResolve = effectsToResolve;
        this.xValue = 0;
        this.targetPermanentId = targetPermanentId;
        this.sourcePermanentId = null;
        this.damageAssignments = Map.of();
        this.targetZone = targetZone;
        this.targetCardIds = List.of();
        this.targetFilter = null;
        this.targetPermanentIds = List.of();
    }

    // Spell copy constructor - preserves all fields from the original stack entry
    public StackEntry(StackEntryType entryType, Card card, UUID controllerId, String description,
                      List<CardEffect> effectsToResolve, int xValue, UUID targetPermanentId,
                      UUID sourcePermanentId, Map<UUID, Integer> damageAssignments,
                      Zone targetZone, List<UUID> targetCardIds, List<UUID> targetPermanentIds) {
        this.entryType = entryType;
        this.card = card;
        this.controllerId = controllerId;
        this.description = description;
        this.effectsToResolve = effectsToResolve;
        this.xValue = xValue;
        this.targetPermanentId = targetPermanentId;
        this.sourcePermanentId = sourcePermanentId;
        this.damageAssignments = damageAssignments != null ? damageAssignments : Map.of();
        this.targetZone = targetZone;
        this.targetCardIds = targetCardIds != null ? targetCardIds : List.of();
        this.targetFilter = null;
        this.targetPermanentIds = targetPermanentIds != null ? targetPermanentIds : List.of();
    }

    // Multi-target triggered ability constructor (e.g. exile up to N cards from graveyards)
    public StackEntry(StackEntryType entryType, Card card, UUID controllerId, String description, List<CardEffect> effectsToResolve, List<UUID> targetCardIds) {
        this.entryType = entryType;
        this.card = card;
        this.controllerId = controllerId;
        this.description = description;
        this.effectsToResolve = effectsToResolve;
        this.xValue = 0;
        this.targetPermanentId = null;
        this.sourcePermanentId = null;
        this.damageAssignments = Map.of();
        this.targetZone = null;
        this.targetCardIds = targetCardIds != null ? targetCardIds : List.of();
        this.targetFilter = null;
        this.targetPermanentIds = List.of();
    }

    /**
     * Copy constructor for deep-copying game state during AI simulation.
     * Card and CardEffect references are shared (immutable after construction).
     */
    public StackEntry(StackEntry source) {
        this.entryType = source.entryType;
        this.card = source.card;
        this.controllerId = source.controllerId;
        this.description = source.description;
        this.effectsToResolve = new ArrayList<>(source.effectsToResolve);
        this.xValue = source.xValue;
        this.targetPermanentId = source.targetPermanentId;
        this.sourcePermanentId = source.sourcePermanentId;
        this.damageAssignments = source.damageAssignments.isEmpty() ? Map.of() : new HashMap<>(source.damageAssignments);
        this.targetZone = source.targetZone;
        this.targetCardIds = source.targetCardIds.isEmpty() ? List.of() : new ArrayList<>(source.targetCardIds);
        this.targetFilter = source.targetFilter;
        this.copy = source.copy;
        this.nonTargeting = source.nonTargeting;
        this.returnToHandAfterResolving = source.returnToHandAfterResolving;
        this.castWithFlashback = source.castWithFlashback;
        this.damageSourceCard = source.damageSourceCard;
        this.targetPermanentIds = source.targetPermanentIds.isEmpty() ? List.of() : new ArrayList<>(source.targetPermanentIds);
    }

    // Multi-target permanent spell constructor (e.g. "one or two target creatures")
    public StackEntry(StackEntryType entryType, Card card, UUID controllerId, String description, List<CardEffect> effectsToResolve, int xValue, List<UUID> targetPermanentIds) {
        this.entryType = entryType;
        this.card = card;
        this.controllerId = controllerId;
        this.description = description;
        this.effectsToResolve = effectsToResolve;
        this.xValue = xValue;
        this.targetPermanentId = null;
        this.sourcePermanentId = null;
        this.damageAssignments = Map.of();
        this.targetZone = null;
        this.targetCardIds = List.of();
        this.targetFilter = null;
        this.targetPermanentIds = targetPermanentIds != null ? targetPermanentIds : List.of();
    }

    /**
     * Returns the card to use as the damage source for protection and prevention checks.
     * Normally this is the same as {@link #getCard()}, but for equipment-granted abilities
     * like Blazing Torch the damage source is the equipment, not the equipped creature.
     */
    public Card getEffectiveDamageSourceCard() {
        return damageSourceCard != null ? damageSourceCard : card;
    }
}
