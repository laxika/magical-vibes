package com.github.laxika.magicalvibes.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CardSubtype {

    FOREST("Forest"),
    MOUNTAIN("Mountain"),
    ISLAND("Island"),
    PLAINS("Plains"),
    SWAMP("Swamp"),
    ANGEL("Angel"),
    WALL("Wall"),
    BEAR("Bear"),
    ELF("Elf"),
    DRUID("Druid"),
    SPIDER("Spider"),
    BEAST("Beast");

    @Getter
    private final String displayName;
}
