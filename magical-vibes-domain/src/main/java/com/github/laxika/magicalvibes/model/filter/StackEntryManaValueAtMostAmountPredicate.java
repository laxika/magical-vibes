package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.Objects;

/**
 * Matches a spell on the stack whose mana value (with its chosen X, 0 for a face-down spell) is at most
 * {@code amount}, evaluated for the player judging the predicate: "spells you cast with mana value X or less, where X
 * is the total amount of life your opponents have lost this turn" (Abaddon the Despoiler).
 */
public record StackEntryManaValueAtMostAmountPredicate(DynamicAmount amount) implements StackEntryPredicate {

    public StackEntryManaValueAtMostAmountPredicate {
        Objects.requireNonNull(amount, "amount");
    }
}
