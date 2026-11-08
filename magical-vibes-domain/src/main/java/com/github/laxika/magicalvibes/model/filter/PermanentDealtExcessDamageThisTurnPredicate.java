package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents that have been dealt excess damage this turn (CR 120.10): Maarika, Brutal Gladiator's "if that
 * creature was dealt excess damage this turn", the intervening "if"
 * {@code TriggeringPermanentMatches(new PermanentDealtExcessDamageThisTurnPredicate())}. Evaluated against
 * {@code GameData.permanentsDealtExcessDamageThisTurn}, so a permanent that has since left the battlefield matches by its
 * last-known identity.
 */
public record PermanentDealtExcessDamageThisTurnPredicate() implements PermanentPredicate {
}
