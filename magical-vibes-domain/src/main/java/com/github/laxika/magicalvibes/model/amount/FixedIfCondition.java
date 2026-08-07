package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.condition.Condition;

/**
 * Evaluates to {@code amount} when {@code condition} is met at evaluation time, and to
 * {@code otherwise} when it is not — the generic bridge from the {@code Condition} hierarchy into
 * the {@code DynamicAmount} one.
 *
 * <p>Models any "… N …. [Condition] — … M instead" rider whose only difference is a number, so the
 * effect stays a single record parameterized by an amount. Gather the Pack feeds it as the
 * {@code chooseCount} of a {@code LookAtTopCardsEffect}: spell mastery raises the number of
 * revealed creature cards kept from one to two.
 */
public record FixedIfCondition(Condition condition, int amount, int otherwise)
        implements DynamicAmount {
}
