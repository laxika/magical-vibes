package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents that have been dealt damage this turn (from any source — combat, spells, abilities).
 * Evaluated against {@code GameData.permanentsDealtDamageThisTurn}.
 */
public record PermanentDealtDamageThisTurnPredicate() implements PermanentPredicate {
}
