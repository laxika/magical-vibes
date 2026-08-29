package com.github.laxika.magicalvibes.model.effect;

/**
 * Who loses life when a {@link LoseLifeEffect} resolves.
 *
 * <ul>
 *   <li>{@link #CONTROLLER} — the effect's controller loses life (a cost/drawback, e.g. Phyrexian
 *       Rager).</li>
 *   <li>{@link #TARGET_PLAYER} — the targeted player (stack entry's {@code targetId}) loses life;
 *       the effect targets a player.</li>
 *   <li>{@link #TRIGGERING_PLAYER} — the player whose spell or event caused the trigger
 *       (stack entry's {@code targetId}) loses life; the effect does not target that player.</li>
 *   <li>{@link #TARGET_PERMANENT_CONTROLLER} — the controller of the targeted permanent (stack
 *       entry's {@code targetId} is a permanent, not a player) loses life; the effect does not add
 *       its own target. Used by "destroy target creature; ... its controller loses N life"
 *       (Soul Reap). Resolve before any accompanying destroy so the permanent is still present.</li>
 *   <li>{@link #DYING_CREATURE_CONTROLLER} — the last-known controller of the creature that died,
 *       for {@code ON_DAMAGED_CREATURE_DIES} triggers such as "whenever a creature dealt damage by
 *       that creature dies this turn, its controller loses 2 life" (Touch of Moonglove). The
 *       graveyard pipeline binds that player onto the trigger's {@code targetId} when the ability is
 *       put on the stack; the effect chooses no target.</li>
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
    TRIGGERING_PLAYER,
    TARGET_PERMANENT_CONTROLLER,
    DYING_CREATURE_CONTROLLER,
    DEFENDING_PLAYER,
    /** The player whose upkeep is currently resolving; supplied by the trigger entry. */
    ACTIVE_PLAYER,
    EACH_PLAYER,
    EACH_OPPONENT
}
