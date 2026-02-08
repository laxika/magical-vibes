package com.github.laxika.magicalvibes.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CardType {

    BASIC_LAND("Basic Land"),
    CREATURE("Creature");

    @JsonValue
    @Getter
    private final String displayName;
}
