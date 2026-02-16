package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import lombok.Getter;
import lombok.Setter;

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
    private final TargetZone targetZone;
    private final List<UUID> targetCardIds;
    @Setter private boolean copy;

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
    }

    // Zone-aware targeted ability constructor (e.g. target a card in graveyard)
    public StackEntry(StackEntryType entryType, Card card, UUID controllerId, String description, List<CardEffect> effectsToResolve, UUID targetPermanentId, TargetZone targetZone) {
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
    }

    // Spell copy constructor — preserves all fields from the original stack entry
    public StackEntry(StackEntryType entryType, Card card, UUID controllerId, String description,
                      List<CardEffect> effectsToResolve, int xValue, UUID targetPermanentId,
                      UUID sourcePermanentId, Map<UUID, Integer> damageAssignments,
                      TargetZone targetZone, List<UUID> targetCardIds) {
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
    }
}
