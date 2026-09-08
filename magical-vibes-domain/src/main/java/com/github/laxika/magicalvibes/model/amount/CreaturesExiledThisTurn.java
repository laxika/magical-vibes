package com.github.laxika.magicalvibes.model.amount;

/** The number of creatures exiled from the battlefield this turn in the requested scope. */
public record CreaturesExiledThisTurn(CountScope scope) implements DynamicAmount {
}
