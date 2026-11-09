package com.github.laxika.magicalvibes.model.amount;

/**
 * The total amount of mana spent to cast the spell this amount belongs to (e.g. Molten
 * Note, Memory Deluge). The engine snapshots the total into the stack entry's xValue at cast
 * time — {@code SpellCastingService} detects this amount via
 * {@code EffectResolution.hasManaSpentToCastAmount} — so evaluation just reads the
 * snapshot. Distinct from {@link XValue} so that other xValue producers (X payments,
 * converge) are not overwritten by the mana-spent snapshot.
 *
 * <p>On a spell-cast trigger it is "the amount of mana spent to cast that spell" (Aetherflux Conduit, Chandra, Dressed to
 * Kill's emblem, Tellah): what the triggering spell recorded as it became cast, read from the entry's snapshot of it as
 * the ability resolves, the same once the spell has left the stack; 0 for a copy that wasn't cast (CR 707.10).
 */
public record ManaSpentToCast() implements DynamicAmount {
}
