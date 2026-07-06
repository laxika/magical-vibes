package com.github.laxika.magicalvibes.model.amount;

/**
 * The integer payload of the trigger event or prior resolution step that produced this stack
 * entry — the amount of life gained, damage dealt, excess damage, etc. Snapshotted onto the
 * {@code StackEntry} by the trigger collector that enqueues it (see {@code referencesEventValue}),
 * or by an earlier effect on the same entry (e.g. excess damage from a damage effect), and read
 * back at resolution as {@code StackEntry.getEventValue()}.
 *
 * <p>Distinct from {@link XValue}: {@code XValue} carries cast-time data (mana spent / X paid),
 * whereas {@code EventValue} carries the numeric result of the event that triggered the ability.
 */
public record EventValue() implements DynamicAmount {
}
