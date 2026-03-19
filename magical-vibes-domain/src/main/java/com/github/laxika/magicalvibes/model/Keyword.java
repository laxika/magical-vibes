package com.github.laxika.magicalvibes.model;

import java.util.Map;

public enum Keyword {

    FLYING,
    REACH,
    DEFENDER,
    DOUBLE_STRIKE,
    FIRST_STRIKE,
    FLASH,
    VIGILANCE,
    SHROUD,
    CHANGELING,
    FEAR,
    MENACE,
    INDESTRUCTIBLE,
    CONVOKE,
    HASTE,
    TRAMPLE,
    LIFELINK,
    FORESTWALK,
    MOUNTAINWALK,
    ISLANDWALK,
    SWAMPWALK,
    HEXPROOF,
    INFECT,
    INTIMIDATE,
    METALCRAFT,
    BATTLE_CRY,
    LIVING_WEAPON,
    DEATHTOUCH,
    SCRY,
    FLASHBACK,
    TRANSFORM,
    KICKER;

    /**
     * Maps each landwalk keyword to the land subtype it walks over.
     */
    public static final Map<Keyword, CardSubtype> LANDWALK_MAP = Map.of(
            FORESTWALK, CardSubtype.FOREST,
            MOUNTAINWALK, CardSubtype.MOUNTAIN,
            ISLANDWALK, CardSubtype.ISLAND,
            SWAMPWALK, CardSubtype.SWAMP
    );
}
