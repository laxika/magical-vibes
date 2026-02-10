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

    // Creature spell constructor
    public StackEntry(Card card, Long controllerId) {
        this.entryType = StackEntryType.CREATURE_SPELL;
        this.card = card;
        this.controllerId = controllerId;
        this.description = card.getName();
        this.effectsToResolve = List.of();
        this.xValue = 0;
    }

    // Triggered ability constructor
    public StackEntry(StackEntryType entryType, Card card, Long controllerId, String description, List<CardEffect> effectsToResolve) {
        this.entryType = entryType;
        this.card = card;
        this.controllerId = controllerId;
        this.description = description;
        this.effectsToResolve = effectsToResolve;
        this.xValue = 0;
    }

    // General constructor with xValue (for sorcery/instant spells)
    public StackEntry(StackEntryType entryType, Card card, Long controllerId, String description, List<CardEffect> effectsToResolve, int xValue) {
        this.entryType = entryType;
        this.card = card;
        this.controllerId = controllerId;
        this.description = description;
        this.effectsToResolve = effectsToResolve;
        this.xValue = xValue;
    }
}
