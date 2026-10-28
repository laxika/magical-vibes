package com.github.laxika.magicalvibes.model;

/** The game event that consumes a player-scoped boon. */
public enum BoonTrigger {
    CREATURE_CAST,
    /** A creature enters under the boon's controller's control. */
    CREATURE_ENTERS,
    /**
     * One or more creatures enter under the boon's controller's control together. Fires once per entering
     * batch (CR 603.2c) rather than once per creature, and the controller chooses which entrant the ability
     * is applied to ("When one or more creatures enter ... attached to one of them", Dunbarrow Revivalist).
     */
    ONE_OR_MORE_CREATURES_ENTER,
    /** A permanent of any kind (creature or not) enters under the boon's controller's control. */
    PERMANENT_ENTERS,
    CONTROLLER_END_STEP
}
