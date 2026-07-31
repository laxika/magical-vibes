package com.github.laxika.magicalvibes.model.amount;

/**
 * The number of times the caster paid one particular option of a
 * {@link com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost} as the spell
 * was cast ("for each additional {1}{G} you paid" — Primitive Justice).
 *
 * <p>The chosen payments are snapshotted onto the {@code StackEntry} at cast time and read back
 * at resolution; {@code manaCost} must be spelled exactly as it appears in the cost's option list.
 * Evaluates to 0 outside a stack entry (e.g. AI estimation).
 */
public record RepeatedAdditionalCostCount(String manaCost) implements DynamicAmount {
}
