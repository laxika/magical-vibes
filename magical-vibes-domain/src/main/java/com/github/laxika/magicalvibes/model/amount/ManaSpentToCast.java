package com.github.laxika.magicalvibes.model.amount;

/**
 * The total amount of mana spent to cast the spell this amount belongs to (e.g. Molten
 * Note, Memory Deluge). The engine snapshots the total into the stack entry's xValue at cast
 * time — {@code SpellCastingService} detects this amount via
 * {@code EffectResolution.hasManaSpentToCastAmount} — so evaluation just reads the
 * snapshot. Distinct from {@link XValue} so that other xValue producers (X payments,
 * converge) are not overwritten by the mana-spent snapshot.
 */
public record ManaSpentToCast() implements DynamicAmount {
}
