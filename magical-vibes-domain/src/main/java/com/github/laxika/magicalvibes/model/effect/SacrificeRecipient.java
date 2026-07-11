package com.github.laxika.magicalvibes.model.effect;

/**
 * Who sacrifices when a {@link SacrificePermanentsEffect} resolves.
 *
 * <ul>
 *   <li>{@link #CONTROLLER} — the effect's controller sacrifices.</li>
 *   <li>{@link #TARGET_PLAYER} — the targeted player (stack entry's {@code targetId}) sacrifices;
 *       the effect targets a player.</li>
 *   <li>{@link #EACH_PLAYER} — every player sacrifices, in APNAP order (CR 101.4 simultaneous).</li>
 *   <li>{@link #EACH_OPPONENT} — every opponent of the controller sacrifices, in APNAP order.</li>
 * </ul>
 */
public enum SacrificeRecipient {
    CONTROLLER,
    TARGET_PLAYER,
    EACH_PLAYER,
    EACH_OPPONENT
}
