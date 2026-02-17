package com.github.laxika.magicalvibes.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CardSupertype {

    BASIC("Basic"),
    LEGENDARY("Legendary");

    @Getter
    private final String displayName;
}
