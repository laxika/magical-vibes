package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.Objects;

/**
 * Matches a card whose mana value is at most {@code amount}, evaluated as the effect reading it resolves: "if the
 * spell's mana value is less than or equal to the amount of life you gained this turn" (Bre of Clan Stoutarm,
 * {@code LifeGainedThisTurn}); "less than the number of Mountains you control" is the Mountain count plus
 * {@code Fixed(-1)} (Solstice Revelations). The resolving handler binds it with
 * {@code AmountEvaluationService.bindAmounts}; unbound it matches nothing.
 */
public record CardManaValueAtMostAmountPredicate(DynamicAmount amount) implements CardPredicate {

    public CardManaValueAtMostAmountPredicate {
        Objects.requireNonNull(amount, "amount");
    }
}
