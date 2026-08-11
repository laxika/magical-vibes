package com.github.laxika.magicalvibes.model.amount;

/**
 * The number of creature cards that entered the battlefield under players in the given scope
 * this turn.
 */
public record CreaturesEnteredBattlefieldThisTurn(CountScope scope) implements DynamicAmount {
}
