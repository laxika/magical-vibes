package com.github.laxika.magicalvibes.model;

/** The game event that consumes a player-scoped boon. */
public enum BoonTrigger {
    CREATURE_CAST,
    /** A creature enters under the boon's controller's control. */
    CREATURE_ENTERS,
    /** A permanent of any kind (creature or not) enters under the boon's controller's control. */
    PERMANENT_ENTERS,
    CONTROLLER_END_STEP
}
