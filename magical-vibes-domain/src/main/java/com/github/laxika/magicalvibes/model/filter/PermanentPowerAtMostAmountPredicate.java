package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.Objects;

/**
 * Matches a permanent whose power is at most {@code amount}, the amount evaluated once as the effect reading it
 * resolves (CR 608.2h): "power less than that creature's power" is the target's power plus {@code Fixed(-1)}
 * (Mandate of Abaddon). The resolving handler binds it with {@code AmountEvaluationService.bindAmounts}; unbound it
 * matches nothing.
 */
public record PermanentPowerAtMostAmountPredicate(DynamicAmount amount) implements PermanentPredicate {

    public PermanentPowerAtMostAmountPredicate {
        Objects.requireNonNull(amount, "amount");
    }
}
