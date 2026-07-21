package com.github.laxika.magicalvibes.model.effect;

/**
 * Who loses life when a {@link LoseLifeEffect} resolves.
 *
 * <ul>
 *   <li>{@link #CONTROLLER} — the effect's controller loses life (a cost/drawback, e.g. Phyrexian
 *       Rager).</li>
 *   <li>{@link #TARGET_PLAYER} — the targeted player (stack entry's {@code targetId}) loses life;
 *       the effect targets a player.</li>
 *   <li>{@link #TARGET_PERMANENT_CONTROLLER} — the controller of the targeted permanent (stack
 *       entry's {@code targetId} is a permanent, not a player) loses life; the effect does not add
 *       its own target. Used by "destroy target creature; ... its controller loses N life"
 *       (Soul Reap). Resolve before any accompanying destroy so the permanent is still present.</li>
 *   <li>{@link #DEFENDING_PLAYER} — the player being attacked by the source (or the controller of
 *       the attacked planeswalker), captured as the trigger's {@code attackedTargetId}; used by
 *       combat triggers such as "whenever this creature becomes blocked, defending player loses N
 *       life" (Vedalken Ghoul). The effect chooses no target.</li>
 *   <li>{@link #EACH_PLAYER} — every player loses life, in {@code orderedPlayerIds} order.</li>
 *   <li>{@link #EACH_OPPONENT} — every opponent of the controller loses life, in
 *       {@code orderedPlayerIds} order.</li>
 * </ul>
 */
public enum LoseLifeRecipient {
    CONTROLLER,
    TARGET_PLAYER,
    TARGET_PERMANENT_CONTROLLER,
    DEFENDING_PLAYER,
    EACH_PLAYER,
    EACH_OPPONENT
}
