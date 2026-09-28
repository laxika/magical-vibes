package com.github.laxika.magicalvibes.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CardSupertype {

    BASIC("Basic"),
    LEGENDARY("Legendary"),
    SNOW("Snow"),
    /** Ongoing scheme supertype (CR 205.4). */
    ONGOING("Ongoing"),
    /** CR 205.4f — subject to the world rule state-based action (CR 704.5k). */
    WORLD("World");

    @Getter
    private final String displayName;
}
