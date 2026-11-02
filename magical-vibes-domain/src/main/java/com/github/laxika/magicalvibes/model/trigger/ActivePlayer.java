package com.github.laxika.magicalvibes.model.trigger;

/**
 * Whose step or phase a {@link StepTrigger} triggers on: how the active player relates to the triggered
 * ability's controller or to the object its source is attached to.
 */
public enum ActivePlayer {
    /** The ability's controller: "at the beginning of your upkeep". */
    YOU,
    /** Any player: "at the beginning of each upkeep", "at end of combat". */
    ANY,
    /** An opponent of the ability's controller: "at the beginning of each opponent's upkeep". */
    OPPONENT,
    /** The player the source enchants, for a Curse: "at the beginning of enchanted player's upkeep". */
    ENCHANTED_PLAYER,
    /**
     * The controller of the permanent the source enchants: "at the beginning of the upkeep of enchanted creature's
     * controller".
     */
    ENCHANTED_PERMANENT_CONTROLLER
}
