package com.github.laxika.magicalvibes.model.effect;

/**
 * Which player is prompted by a {@link MayPayManaEffect} — "you may pay" is not always the
 * ability's controller.
 */
public enum MayPayPayer {

    /** The ability's controller ("you may pay {X}"). */
    CONTROLLER,

    /**
     * The enchanted permanent's controller — the player carried on the stack entry's
     * {@code targetId} by an {@code ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED} trigger
     * ("that player may pay {X}", Paralyze).
     */
    ENCHANTED_CONTROLLER,

    /**
     * The defending player of the attack that triggered the ability — the attacked player, or the
     * controller of the attacked planeswalker, read from the {@code ON_ATTACK} trigger's
     * {@code attackedTargetId} ("defending player may pay {X}", Mtenda Lion).
     */
    DEFENDING_PLAYER
}
