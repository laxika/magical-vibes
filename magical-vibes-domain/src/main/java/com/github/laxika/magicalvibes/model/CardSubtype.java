package com.github.laxika.magicalvibes.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CardSubtype {

    FOREST("Forest"),
    BEAR("Bear"),
    ELF("Elf"),
    DRUID("Druid"),
    SPIDER("Spider");

    @Getter
    private final String displayName;
}
