package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches the permanent the source Aura was attached to when the ability was put on the stack, using
 * the source snapshot taken then. Unlike {@link PermanentIsHostOfSourceAuraPredicate}, a later move of
 * the Aura does not change the match. Used for "other than the creature tapped this way" restrictions
 * (Veteran's Voice), where the creature is fixed by the cost rather than by the Aura's current host.
 */
public record PermanentIsLastKnownHostOfSourceAuraPredicate() implements PermanentPredicate {
}
