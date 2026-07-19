package com.github.laxika.magicalvibes.model.effect;

/**
 * Who sacrifices when a {@link SacrificePermanentsEffect} resolves.
 *
 * <ul>
 *   <li>{@link #CONTROLLER} — the effect's controller sacrifices.</li>
 *   <li>{@link #TARGET_PLAYER} — the targeted player (stack entry's {@code targetId}) sacrifices;
 *       the effect targets a player.</li>
 *   <li>{@link #TARGET_PLAYER_OR_PERMANENT_CONTROLLER} — the targeted player, or the controller of
 *       the targeted planeswalker, sacrifices; the effect does not add its own target (it piggybacks
 *       on a companion "player or planeswalker" damage effect's {@code targetId}). Used by "deals
 *       damage to target player or planeswalker; that player or that planeswalker's controller ...
 *       sacrifices permanents" (Nicol Bolas, Planeswalker).</li>
 *   <li>{@link #EACH_PLAYER} — every player sacrifices, in APNAP order (CR 101.4 simultaneous).</li>
 *   <li>{@link #EACH_OPPONENT} — every opponent of the controller sacrifices, in APNAP order.</li>
 * </ul>
 */
public enum SacrificeRecipient {
    CONTROLLER,
    TARGET_PLAYER,
    TARGET_PLAYER_OR_PERMANENT_CONTROLLER,
    EACH_PLAYER,
    EACH_OPPONENT
}
