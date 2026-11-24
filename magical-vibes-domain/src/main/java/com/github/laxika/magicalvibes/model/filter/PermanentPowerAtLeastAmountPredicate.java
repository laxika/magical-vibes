package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.Objects;

/**
 * Matches a permanent whose power is at least {@code amount}, the amount evaluated once as the effect reading it
 * resolves (CR 608.2h): "power greater than or equal to the chosen number" ({@code XValue} after a
 * {@code ChooseNumberAtResolutionEffect}, Expel the Interlopers); "power greater than target creature's power" is the
 * target's power plus {@code Fixed(1)} (Fell the Mighty). The resolving handler binds it with
 * {@code AmountEvaluationService.bindAmounts}; unbound it matches nothing.
 */
public record PermanentPowerAtLeastAmountPredicate(DynamicAmount amount) implements PermanentPredicate {

    public PermanentPowerAtLeastAmountPredicate {
        Objects.requireNonNull(amount, "amount");
    }
}
