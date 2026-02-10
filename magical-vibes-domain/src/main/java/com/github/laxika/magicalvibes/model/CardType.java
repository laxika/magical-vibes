package com.github.laxika.magicalvibes.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CardType {

    BASIC_LAND("Basic Land"),
    CREATURE("Creature"),
    SORCERY("Sorcery");

    @Getter
    private final String displayName;
}
