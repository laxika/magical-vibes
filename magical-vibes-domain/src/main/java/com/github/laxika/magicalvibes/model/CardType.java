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
    BATTLE("Battle"),
    KINDRED("Kindred"),
    PLANE("Plane"),
    PHENOMENON("Phenomenon");

    @Getter
    private final String displayName;

    public boolean isPermanentType() {
        return switch (this) {
            case LAND, CREATURE, ENCHANTMENT, ARTIFACT, PLANESWALKER, BATTLE -> true;
            default -> false;
        };
    }

    public boolean isPlanar() {
        return this == PLANE || this == PHENOMENON;
    }
}
