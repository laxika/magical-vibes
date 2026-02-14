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
    BEAST("Beast"),
    HUMAN("Human"),
    CLERIC("Cleric"),
    BIRD("Bird"),
    CAT("Cat"),
    SOLDIER("Soldier"),
    REBEL("Rebel"),
    KNIGHT("Knight"),
    SPIRIT("Spirit"),
    AURA("Aura"),
    NOMAD("Nomad"),
    WIZARD("Wizard"),
    MUTANT("Mutant"),
    WOLF("Wolf"),
    MONK("Monk"),
    GRIFFIN("Griffin"),
    SKELETON("Skeleton"),
    ELEPHANT("Elephant"),
    ELEMENTAL("Elemental"),
    MERFOLK("Merfolk"),
    OCTOPUS("Octopus");

    @Getter
    private final String displayName;
}
