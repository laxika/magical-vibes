package com.github.laxika.magicalvibes.model.effect;

/**
 * Whose graveyard an effect looks at.
 *
 * <ul>
 *   <li>{@link #CONTROLLER} — the effect's controller (Mistmoon Griffin, "your graveyard").</li>
 *   <li>{@link #TARGET_PLAYER} — the player recorded as the stack entry's {@code targetId}; on
 *       combat triggers that is the defending player (Bone Dancer).</li>
 * </ul>
 */
public enum GraveyardOwner {
    CONTROLLER,
    TARGET_PLAYER
}
