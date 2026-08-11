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
    TRIGGERING_PLAYER
}
