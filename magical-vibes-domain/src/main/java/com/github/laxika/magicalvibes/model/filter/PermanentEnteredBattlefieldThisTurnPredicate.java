package com.github.laxika.magicalvibes.model.filter;

/**
 * Permanents whose physical card entered the battlefield this turn. The check uses the permanent's
 * original card identity so a copy effect does not erase the fact that the permanent entered this
 * turn; it also accepts the current card identity for transformed permanents.
 */
public record PermanentEnteredBattlefieldThisTurnPredicate() implements PermanentPredicate {
}
