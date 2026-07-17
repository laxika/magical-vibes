package com.github.laxika.magicalvibes.model.effect;

/**
 * Who discards when a {@link DiscardEffect} resolves.
 *
 * <ul>
 *   <li>{@link #CONTROLLER} — the effect's controller discards (self-discard / rummaging;
 *       {@code discardCausedByOpponent = false}).</li>
 *   <li>{@link #TARGET_PLAYER} — the targeted player (stack entry's {@code targetId}) discards;
 *       the effect targets a player and sets {@code discardCausedByOpponent = true}.</li>
 *   <li>{@link #TARGET_PERMANENT_CONTROLLER} — the controller of the targeted permanent (stack
 *       entry's {@code targetId} is a permanent, not a player) discards; the effect does not add its
 *       own target and sets {@code discardCausedByOpponent = true}. Used by "destroy target creature;
 *       ... its controller discards a card" (Gloomlance).</li>
 *   <li>{@link #TARGET_PLAYER_OR_PERMANENT_CONTROLLER} — the targeted player, or the controller of the
 *       targeted planeswalker, discards; the effect does not add its own target (it piggybacks on a
 *       companion "player or planeswalker" damage effect's {@code targetId}) and sets
 *       {@code discardCausedByOpponent = true}. Used by "deals damage to target player or planeswalker;
 *       that player or that planeswalker's controller discards" (Blightning).</li>
 *   <li>{@link #EACH_PLAYER} — every player discards, in APNAP order.</li>
 *   <li>{@link #EACH_OPPONENT} — every opponent of the controller discards, in APNAP order.</li>
 * </ul>
 */
public enum DiscardRecipient {
    CONTROLLER,
    TARGET_PLAYER,
    TARGET_PERMANENT_CONTROLLER,
    TARGET_PLAYER_OR_PERMANENT_CONTROLLER,
    EACH_PLAYER,
    EACH_OPPONENT
}
