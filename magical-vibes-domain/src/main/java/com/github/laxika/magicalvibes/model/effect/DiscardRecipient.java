package com.github.laxika.magicalvibes.model.effect;

/**
 * Who discards when a {@link DiscardEffect} resolves.
 *
 * <ul>
 *   <li>{@link #CONTROLLER} — the effect's controller discards (self-discard / rummaging;
 *       {@code discardCausedByOpponent = false}).</li>
 *   <li>{@link #TARGET_PLAYER} — the targeted player (stack entry's {@code targetId}) discards;
 *       the effect targets a player and sets {@code discardCausedByOpponent = true}.</li>
 *   <li>{@link #EACH_PLAYER} — every player discards, in APNAP order.</li>
 *   <li>{@link #EACH_OPPONENT} — every opponent of the controller discards, in APNAP order.</li>
 * </ul>
 */
public enum DiscardRecipient {
    CONTROLLER,
    TARGET_PLAYER,
    EACH_PLAYER,
    EACH_OPPONENT
}
