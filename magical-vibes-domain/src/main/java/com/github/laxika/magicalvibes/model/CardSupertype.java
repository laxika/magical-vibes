package com.github.laxika.magicalvibes.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CardSupertype {

    BASIC("Basic"),
    LEGENDARY("Legendary"),
    SNOW("Snow"),
    /** CR 205.4f — subject to the world rule state-based action (CR 704.5k). */
    WORLD("World");

    @Getter
    private final String displayName;
}
