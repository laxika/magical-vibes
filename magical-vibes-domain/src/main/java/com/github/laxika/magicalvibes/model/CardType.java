package com.github.laxika.magicalvibes.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CardType {

    LAND("Land"),
    CREATURE("Creature"),
    ENCHANTMENT("Enchantment"),
    SORCERY("Sorcery"),
    INSTANT("Instant"),
    ARTIFACT("Artifact"),
    PLANESWALKER("Planeswalker"),
    KINDRED("Kindred");

    @Getter
    private final String displayName;

    public boolean isPermanentType() {
        return this != INSTANT && this != SORCERY;
    }
}
