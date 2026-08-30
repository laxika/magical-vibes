package com.github.laxika.magicalvibes.model.effect;

/**
 * Which player is prompted by a {@link MayPayManaEffect} — "you may pay" is not always the
 * ability's controller.
 */
public enum MayPayPayer {

    /** The ability's controller ("you may pay {X}"). */
    CONTROLLER,

    /** Any player, offered in APNAP order ("if any player pays {X}"). */
    ANY_PLAYER,

    /** Any player other than the player whose spell caused the trigger, offered in APNAP order. */
    ANY_OTHER_PLAYER,

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
    DEFENDING_PLAYER,

    /**
     * The controller of the permanent the spell or ability targets — read from the stack entry's
     * {@code targetId} ("that creature's controller may pay {X}", Chain Stasis). Unlike
     * {@link #ENCHANTED_CONTROLLER}, the {@code targetId} is a permanent, not a player.
     */
    TARGET_PERMANENT_CONTROLLER,

    /** The target player, or the controller of the target permanent, may pay. */
    TARGET_PLAYER_OR_PERMANENT_CONTROLLER,

    /** The player whose action caused the trigger, carried on the stack entry's {@code targetId}. */
    TRIGGERING_PLAYER,

    /** The controller of the spell that caused the trigger. */
    TRIGGERING_SPELL_CONTROLLER
}
