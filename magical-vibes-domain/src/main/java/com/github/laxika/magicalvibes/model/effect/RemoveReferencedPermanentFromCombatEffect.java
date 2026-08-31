package com.github.laxika.magicalvibes.model.effect;

/**
 * Removes the permanent named by a {@link PermanentReference} from combat without targeting it.
 * The reference is resolved when the effect resolves, so a missing permanent is a no-op.
 */
public record RemoveReferencedPermanentFromCombatEffect(PermanentReference reference) implements CardEffect {
}
