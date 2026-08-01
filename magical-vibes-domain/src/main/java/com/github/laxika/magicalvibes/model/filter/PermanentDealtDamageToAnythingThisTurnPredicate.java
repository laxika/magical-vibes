package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents that dealt damage — combat or noncombat, to any player or creature — this turn
 * ("target creature that dealt damage this turn", Avenging Arrow).
 *
 * <p>Evaluated against {@code GameData.combatDamageToPlayersThisTurn} and
 * {@code GameData.noncombatDamageToPlayersThisTurn} (damage to players) plus
 * {@code GameData.creatureCardsDamagedThisTurnBySourcePermanent} (damage to creatures), all keyed by
 * the candidate permanent's id.
 *
 * <p>Not to be confused with {@link PermanentDealtDamageThisTurnPredicate}, which matches permanents
 * that <em>were</em> dealt damage, or {@link PermanentDealtDamageToSourceControllerThisTurnPredicate},
 * which narrows the victim to the source's controller.
 */
public record PermanentDealtDamageToAnythingThisTurnPredicate() implements PermanentPredicate {
}
