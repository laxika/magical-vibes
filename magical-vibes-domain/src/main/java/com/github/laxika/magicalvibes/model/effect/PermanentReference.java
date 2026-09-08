package com.github.laxika.magicalvibes.model.effect;

/**
 * Names a permanent an effect acts on without targeting it. Used by
 * {@link PutCounterOnReferencedPermanentEffect}, {@link DestroyReferencedPermanentEffect}, and
 * {@link RemoveReferencedPermanentFromCombatEffect}.
 *
 * <ul>
 *   <li>{@link #SOURCE} — the permanent the ability came from ("destroy this Aura", "destroy this
 *       enchantment"). Read from {@code StackEntry.sourcePermanentId}. Not every family accepts
 *       this value: counter placement on the source is owned by {@code PutCountersOnSourceEffect},
 *       so {@link PutCounterOnReferencedPermanentEffect} rejects it in its constructor.</li>
 *   <li>{@link #ATTACHED} — the permanent the source Aura or Equipment is attached to ("enchanted
 *       creature", "equipped creature"). Read from the source permanent's {@code attachedTo}, so
 *       Aura and Equipment are the same case: both resolve through {@code Permanent.getAttachedTo()}
 *       and neither targets, because the enchant/equip clause already chose the host.</li>
 *   <li>{@link #TRIGGERING} — the permanent whose event produced this triggered ability ("put a
 *       wind counter on it", where "it" is the permanent that became tapped; Freyalise's Winds).
 *       Read from {@code StackEntry.triggeringPermanentId}, so an effect using this value belongs
 *       only on a trigger slot that populates it.</li>
 *   <li>{@link #RETURNED} — the permanent created by a preceding targeted graveyard return. Read
 *       from the graveyard card ID in {@code StackEntry.targetId} or its targeted-card ID list;
 *       the reference is unresolved when the card was not returned or has already left the
 *       battlefield.</li>
 * </ul>
 *
 * <p>No value ever fizzles: if the referenced permanent has left the battlefield, nothing happens.
 */
public enum PermanentReference {
    SOURCE,
    ATTACHED,
    TRIGGERING,
    RETURNED
}
