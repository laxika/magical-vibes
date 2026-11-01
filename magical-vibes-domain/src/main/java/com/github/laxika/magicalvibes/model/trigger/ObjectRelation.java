package com.github.laxika.magicalvibes.model.trigger;

/**
 * How the object an event happens to relates to the triggered ability's source. It replaces the
 * {@code SELF}, {@code ALLY}, {@code ANY} and {@code OPPONENT} parts of the slot names.
 */
public enum ObjectRelation {
    /** The source itself: "when this creature dies". */
    THIS,
    /** An object its controller controls, the source included if it matches: "a creature you control". */
    YOU_CONTROL,
    /** Any object, the source included if it matches: "a creature". */
    ANY,
    /** An object its controller controls other than the source: "another creature you control". */
    ANOTHER_YOU_CONTROL,
    /** Any object other than the source: "another creature". */
    ANOTHER,
    /**
     * The source, whatever its characteristics, or another object its controller controls that matches:
     * "this creature or another creature you control".
     */
    THIS_OR_ANOTHER_YOU_CONTROL,
    /** The source, whatever its characteristics, or another object that matches: "this creature or another Cleric". */
    THIS_OR_ANOTHER,
    /** An object an opponent of its controller controls. */
    OPPONENT_CONTROLS,
    /** The permanent the source enchants. */
    ENCHANTED,
    /** The permanent the source equips. */
    EQUIPPED,
    /**
     * An object the player the source enchants controls, for a Curse: "a nontoken creature enchanted player
     * controls".
     */
    ENCHANTED_PLAYER_CONTROLS,
    /**
     * The object a card in exile haunts (CR 702.55b), whether or not it is still a creature. Such an ability
     * functions from exile (CR 702.55c).
     */
    HAUNTED,
    /** An object the source dealt damage to this turn: "a creature dealt damage by this creature this turn". */
    DAMAGED_BY_THIS,
    /**
     * An object the permanent the source is attached to dealt damage to this turn: "a creature dealt damage by
     * equipped creature this turn", "by enchanted creature".
     */
    DAMAGED_BY_ATTACHED;

    /** Whether the source itself can be the object. */
    public boolean includesThis() {
        return this == THIS || this == YOU_CONTROL || this == ANY
                || this == THIS_OR_ANOTHER_YOU_CONTROL || this == THIS_OR_ANOTHER;
    }

    /** Whether the object must be one its controller controls: the "you control" relations. */
    public boolean youControl() {
        return this == YOU_CONTROL || this == ANOTHER_YOU_CONTROL || this == THIS_OR_ANOTHER_YOU_CONTROL;
    }

    /** Whether the source itself is always the object, whatever the trigger's filter says: "this or another". */
    public boolean thisAlwaysMatches() {
        return this == THIS || this == THIS_OR_ANOTHER_YOU_CONTROL || this == THIS_OR_ANOTHER;
    }
}
