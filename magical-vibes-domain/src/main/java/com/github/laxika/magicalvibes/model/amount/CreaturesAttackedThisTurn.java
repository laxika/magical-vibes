package com.github.laxika.magicalvibes.model.amount;

/** The cumulative number of creatures declared as attackers by players in scope this turn. */
public record CreaturesAttackedThisTurn(CountScope scope) implements DynamicAmount {
}
