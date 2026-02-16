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
    FAERIE("Faerie"),
    MERFOLK("Merfolk"),
    OCTOPUS("Octopus"),
    SERPENT("Serpent"),
    SHAPESHIFTER("Shapeshifter"),
    CRAB("Crab"),
    DJINN("Djinn"),
    ROGUE("Rogue"),
    ILLUSION("Illusion"),
    DRAKE("Drake"),
    WARRIOR("Warrior"),
    METATHRAN("Metathran"),
    VAMPIRE("Vampire"),
    NOBLE("Noble"),
    PHYREXIAN("Phyrexian"),
    KITHKIN("Kithkin"),
    AJANI("Ajani"),
    WRAITH("Wraith"),
    GNOME("Gnome"),
    MERCENARY("Mercenary"),
    ZOMBIE("Zombie"),
    CROCODILE("Crocodile"),
    CONSTRUCT("Construct"),
    GOLEM("Golem"),
    IMP("Imp"),
    VEDALKEN("Vedalken"),
    GOBLIN("Goblin");

    @Getter
    private final String displayName;
}
