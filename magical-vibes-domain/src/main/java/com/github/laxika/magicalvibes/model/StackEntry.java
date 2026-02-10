package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import lombok.Getter;

import java.util.List;

@Getter
public class StackEntry {

    private final StackEntryType entryType;
    private final Card card;
    private final Long controllerId;
    private final String description;
    private final List<CardEffect> effectsToResolve;
    private final int xValue;
    private final int targetPermanentId;

    // Creature spell constructor
    public StackEntry(Card card, Long controllerId) {
        this.entryType = StackEntryType.CREATURE_SPELL;
        this.card = card;
        this.controllerId = controllerId;
        this.description = card.getName();
        this.effectsToResolve = List.of();
        this.xValue = 0;
        this.targetPermanentId = -1;
    }

    // Triggered ability constructor
    public StackEntry(StackEntryType entryType, Card card, Long controllerId, String description, List<CardEffect> effectsToResolve) {
        this.entryType = entryType;
        this.card = card;
        this.controllerId = controllerId;
        this.description = description;
        this.effectsToResolve = effectsToResolve;
        this.xValue = 0;
        this.targetPermanentId = -1;
    }

    // General constructor with xValue (for sorcery spells)
    public StackEntry(StackEntryType entryType, Card card, Long controllerId, String description, List<CardEffect> effectsToResolve, int xValue) {
        this.entryType = entryType;
        this.card = card;
        this.controllerId = controllerId;
        this.description = description;
        this.effectsToResolve = effectsToResolve;
        this.xValue = xValue;
        this.targetPermanentId = -1;
    }

    // Targeted spell constructor (for instants with targets)
    public StackEntry(StackEntryType entryType, Card card, Long controllerId, String description, List<CardEffect> effectsToResolve, int xValue, int targetPermanentId) {
        this.entryType = entryType;
        this.card = card;
        this.controllerId = controllerId;
        this.description = description;
        this.effectsToResolve = effectsToResolve;
        this.xValue = xValue;
        this.targetPermanentId = targetPermanentId;
    }
}
