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
    EQUIPPED;

    /** Whether the source itself can be the object. */
    public boolean includesThis() {
        return this != ANOTHER_YOU_CONTROL && this != ANOTHER && this != OPPONENT_CONTROLS
                && this != ENCHANTED && this != EQUIPPED;
    }
}
