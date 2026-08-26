package com.github.laxika.magicalvibes.model.amount;

/** The number of creatures that left the battlefield under players in the given scope this turn. */
public record CreaturesLeftBattlefieldThisTurn(CountScope scope) implements DynamicAmount {
}
