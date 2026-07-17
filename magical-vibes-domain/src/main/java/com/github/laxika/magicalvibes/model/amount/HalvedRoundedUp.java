package com.github.laxika.magicalvibes.model.amount;

/**
 * A base amount halved and rounded up (e.g. "loses half their life, rounded up" =
 * {@code HalvedRoundedUp(TargetPlayerLifeTotal)}). The round-up sibling of {@link Divided} — where
 * {@code Divided(amount, 2)} floors, this ceils. Composable over any {@link DynamicAmount}.
 */
public record HalvedRoundedUp(DynamicAmount amount) implements DynamicAmount {
}
