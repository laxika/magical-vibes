package com.github.laxika.magicalvibes.model.effect;

/**
 * Who destroys their own permanents when a {@link PlayerDestroysPermanentsEffect} resolves.
 *
 * <ul>
 *   <li>{@link #CONTROLLER} — the effect's controller chooses and destroys their own permanents.</li>
 *   <li>{@link #TARGET_PLAYER} — the targeted player (stack entry's {@code targetId}) chooses and
 *       destroys their own permanents; the effect targets a player.</li>
 * </ul>
 */
public enum DestroyRecipient {
    CONTROLLER,
    TARGET_PLAYER
}
