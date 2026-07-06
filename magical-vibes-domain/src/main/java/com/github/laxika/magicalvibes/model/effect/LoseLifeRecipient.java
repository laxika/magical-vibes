package com.github.laxika.magicalvibes.model.effect;

/**
 * Who loses life when a {@link LoseLifeEffect} resolves.
 *
 * <ul>
 *   <li>{@link #CONTROLLER} — the effect's controller loses life (a cost/drawback, e.g. Phyrexian
 *       Rager).</li>
 *   <li>{@link #TARGET_PLAYER} — the targeted player (stack entry's {@code targetId}) loses life;
 *       the effect targets a player.</li>
 *   <li>{@link #EACH_PLAYER} — every player loses life, in {@code orderedPlayerIds} order.</li>
 *   <li>{@link #EACH_OPPONENT} — every opponent of the controller loses life, in
 *       {@code orderedPlayerIds} order.</li>
 * </ul>
 */
public enum LoseLifeRecipient {
    CONTROLLER,
    TARGET_PLAYER,
    EACH_PLAYER,
    EACH_OPPONENT
}
