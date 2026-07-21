package com.github.laxika.magicalvibes.model.amount;

/**
 * The total life lost this turn (from any source — direct loss and damage; damage causes loss of
 * life, CR 118.2), summed over the players in scope. Reads {@code GameData.lifeLostThisTurn}, which
 * accumulates per-player life loss and is cleared at the start of each turn. Used by Tainted Sigil
 * ("you gain life equal to the total life lost by all players this turn" — {@link CountScope#ANY_PLAYER}).
 */
public record LifeLostThisTurn(CountScope scope) implements DynamicAmount {
}
