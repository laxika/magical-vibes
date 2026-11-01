package com.github.laxika.magicalvibes.model.trigger;

/** How a player an event names relates to the triggered ability's controller. */
public enum PlayerRelation {
    /** Any player, or none. */
    ANY,
    /** The ability's controller. */
    YOU,
    /** An opponent of the ability's controller. */
    OPPONENT
}
