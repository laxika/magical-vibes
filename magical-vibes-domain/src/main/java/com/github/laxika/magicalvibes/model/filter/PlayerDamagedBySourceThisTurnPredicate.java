package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches players that were dealt damage by the ability's own source permanent this turn — combat
 * or noncombat, any amount ("target player dealt damage by this creature this turn", Wicked Akuba).
 * Evaluated against {@code GameData.combatDamageToPlayersThisTurn} and
 * {@code GameData.noncombatDamageToPlayersThisTurn}, both keyed by source permanent id.
 *
 * <p>Source-relative: the targeting services must know which permanent the ability came from. A
 * targeting path that can't supply that source matches no player, so an unsupported path rejects
 * targets instead of allowing illegal ones.
 */
public record PlayerDamagedBySourceThisTurnPredicate() implements PlayerPredicate {
}
