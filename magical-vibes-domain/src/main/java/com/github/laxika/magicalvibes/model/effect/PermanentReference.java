package com.github.laxika.magicalvibes.model.effect;

/**
 * Names a permanent an effect acts on without targeting it. Used by the effects that have not yet taken a
 * {@link PermanentSubject}; {@link Subjects#of(PermanentReference)} adapts it.
 *
 * <ul>
 *   <li>{@link #SOURCE} — the permanent the ability came from ("remove it from combat", "shuffle this
 *       permanent into its owner's library"). Read from {@code StackEntry.sourcePermanentId}.</li>
 *   <li>{@link #ATTACHED} — the permanent the source Aura or Equipment is attached to ("enchanted
 *       creature", "equipped creature"). Read from the source permanent's {@code attachedTo}, so
 *       Aura and Equipment are the same case: both resolve through {@code Permanent.getAttachedTo()}
 *       and neither targets, because the enchant/equip clause already chose the host.</li>
 *   <li>{@link #ATTACHED_AT_TRIGGER} — the permanent the source was attached to when the ability
 *       triggered or was activated ("that creature" after "at the beginning of the upkeep of enchanted
 *       creature's controller"; Paroxysm, Venarian Gold). Read from
 *       {@code StackEntry.attachedPermanentSnapshot}, so moving the Aura in response doesn't change it.</li>
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
    ATTACHED_AT_TRIGGER,
    TRIGGERING,
    RETURNED
}
