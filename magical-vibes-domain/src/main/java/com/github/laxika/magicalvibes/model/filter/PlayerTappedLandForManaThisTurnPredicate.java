package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a player who tapped a land for mana this turn ("each player who tapped a land for mana this turn", Desolation),
 * whatever was on the battlefield when they did. Evaluated against {@code GameData.playersWhoTappedLandForManaThisTurn}.
 */
public record PlayerTappedLandForManaThisTurnPredicate() implements PlayerPredicate {
}
