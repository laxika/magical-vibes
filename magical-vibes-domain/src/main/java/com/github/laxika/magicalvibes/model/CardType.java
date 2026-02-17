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
    PLANESWALKER("Planeswalker");

    @Getter
    private final String displayName;

}
