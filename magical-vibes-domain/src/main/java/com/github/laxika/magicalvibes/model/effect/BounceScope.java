package com.github.laxika.magicalvibes.model.effect;

/**
 * Selects which permanent(s) a {@link ReturnToHandEffect} returns to hand.
 *
 * <ul>
 *   <li>{@link #TARGET} — the effect's chosen target permanent(s) (reads the stack entry's
 *       {@code targetId}/{@code targetIds}).</li>
 *   <li>{@link #SELF} — the source permanent itself (reads {@code sourcePermanentId}).</li>
 *   <li>{@link #ALL_MATCHING} — every permanent on the battlefield matching the effect's filter
 *       (all battlefields; a null filter matches all permanents).</li>
 *   <li>{@link #TARGET_PLAYERS_PERMANENTS} — every permanent the targeted player <em>controls</em>
 *       matching the filter (reads their battlefield).</li>
 *   <li>{@link #TARGET_PLAYERS_OWNED} — every permanent the targeted player <em>owns</em> matching
 *       the filter, regardless of who controls it (owner-based, honours theft via
 *       {@code stolenCreatures}).</li>
 * </ul>
 */
public enum BounceScope {
    TARGET,
    SELF,
    ALL_MATCHING,
    TARGET_PLAYERS_PERMANENTS,
    TARGET_PLAYERS_OWNED
}
