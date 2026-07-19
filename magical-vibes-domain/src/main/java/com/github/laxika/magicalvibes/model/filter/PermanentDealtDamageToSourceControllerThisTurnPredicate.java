package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents that dealt damage — combat or noncombat — to the source's controller this turn
 * ("target creature that dealt damage to you this turn", Giltspire Avenger).
 *
 * <p>Evaluated against {@code GameData.combatDamageToPlayersThisTurn} (combat) and
 * {@code GameData.noncombatDamageToPlayersThisTurn} (spells/abilities), keyed by the candidate
 * permanent, checking whether the source controller appears among the players it damaged.
 */
public record PermanentDealtDamageToSourceControllerThisTurnPredicate() implements PermanentPredicate {
}
