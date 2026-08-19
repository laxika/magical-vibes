package com.github.laxika.magicalvibes.model.effect;

/**
 * Selects which permanent(s) a {@link ReturnToHandEffect} returns to hand.
 *
 * <ul>
 *   <li>{@link #TARGET} — the effect's chosen target permanent(s) (reads the stack entry's
 *       {@code targetId}/{@code targetIds}).</li>
 *   <li>{@link #SELF} — the source permanent itself (reads {@code sourcePermanentId}).</li>
 *   <li>{@link #SELF_SPELL} — the resolving spell card itself: instead of moving to the graveyard
 *       after resolution it returns to its owner's hand (sets {@code returnToHandAfterResolving}).
 *       For instants/sorceries that return themselves from the stack (Redeem the Lost).</li>
 *   <li>{@link #ALL_MATCHING} — every permanent on the battlefield matching the effect's filter
 *       (all battlefields; a null filter matches all permanents).</li>
 *   <li>{@link #TARGET_PLAYERS_PERMANENTS} — every permanent the targeted player <em>controls</em>
 *       matching the filter (reads their battlefield).</li>
 *   <li>{@link #TARGET_PLAYERS_OWNED} — every permanent the targeted player <em>owns</em> matching
 *       the filter, regardless of who controls it (owner-based, honours theft via
 *       {@code stolenCreatures}).</li>
 *   <li>{@link #AURAS_ATTACHED_TO_TARGET} — every Aura attached to the targeted permanent,
 *       regardless of who controls those Auras (Scarab of the Unseen).</li>
 *   <li>{@link #ENCHANTED} — the permanent the source Aura is attached to (Sun Clasp —
 *       "return enchanted creature to its owner's hand").</li>
 *   <li>{@link #GRANTING_EQUIPMENT} — the Equipment that granted the resolving ability,
 *       captured when the ability was activated.</li>
 * </ul>
 */
public enum BounceScope {
    TARGET,
    TARGET_CHOSEN_CREATURE_TYPE,
    SELF,
    TRIGGERING,
    SELF_SPELL,
    ALL_MATCHING,
    TARGET_PLAYERS_PERMANENTS,
    TARGET_PLAYERS_OWNED,
    AURAS_ATTACHED_TO_TARGET,
    ENCHANTED,
    GRANTING_EQUIPMENT
}
