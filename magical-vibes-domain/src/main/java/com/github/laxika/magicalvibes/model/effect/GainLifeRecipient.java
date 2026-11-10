package com.github.laxika.magicalvibes.model.effect;

/** Who gains the life applied by a {@link GainLifeEffect}. */
public enum GainLifeRecipient {
    /** The controller of the spell/ability (the default for almost every card). */
    CONTROLLER,
    /** The controller of the effect's target permanent (e.g. Condemn: "its controller gains life"). */
    TARGET_CONTROLLER,
    /**
     * The resolving controller's opponent ("target opponent gains N life"; Phelddagrif). This engine
     * is two-player, so the opponent is derived rather than chosen, leaving the entry's target slot
     * free — the same approach {@link TargetOpponentMayDrawCardEffect} takes.
     */
    OPPONENT,
    /** The player whose action caused the trigger ("that player"; Aven Shrine). */
    /** The player recorded as the trigger's target channel, without making the effect targeted. */
    TRIGGERING_PLAYER,
    /**
     * The controller of the stack entry's triggering permanent as the ability resolves, or as it last existed; for a
     * spell or card source, its controller as the event happened ("its controller gains that much life"; Essence
     * Sliver).
     */
    TRIGGERING_PERMANENT_CONTROLLER,
    /**
     * The player the source remembered as it entered ("that player" of Laquatus's Champion's "When this leaves the
     * battlefield, that player gains 6 life"): read from the source as it last existed, or, while the enters ability
     * that chooses the player is still on the stack, that ability's target.
     */
    SOURCE_REMEMBERED_PLAYER,
    /** Every player in the game. */
    EACH_PLAYER
}
