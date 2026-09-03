package com.github.laxika.magicalvibes.model;

/** Identifies who makes a resolution-time {@code MayEffect} choice. */
public enum MayChoicePlayer {
    CONTROLLER,
    ACTIVE_PLAYER,
    DEFENDING_PLAYER,
    TARGET_PLAYER,
    TARGET_PERMANENT_CONTROLLER,
    TARGET_SPELL_CONTROLLER,
    TRIGGERING_PERMANENT_CONTROLLER,
    TARGET_PLAYER_OR_PERMANENT_CONTROLLER,
    TRIGGERING_SPELL_CONTROLLER
}
