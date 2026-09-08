package com.github.laxika.magicalvibes.model.amount;

/**
 * The source permanent's mana value minus one at evaluation time.
 *
 * <p>When evaluated for a death trigger, the source permanent is the last-known battlefield
 * snapshot. The result is intentionally allowed to be negative so a mana-value maximum can
 * represent a strict lesser-than restriction for a zero-mana source.</p>
 */
public record SourceManaValueMinusOne() implements DynamicAmount {
}
