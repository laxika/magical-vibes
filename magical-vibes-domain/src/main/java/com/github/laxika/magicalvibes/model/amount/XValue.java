package com.github.laxika.magicalvibes.model.amount;

/**
 * The snapshotted numeric context of the resolving stack entry ({@code StackEntry.xValue}) —
 * e.g. the mana spent to cast the triggering spell, or an X paid in a cost.
 */
public record XValue() implements DynamicAmount {
}
