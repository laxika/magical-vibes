package com.github.laxika.magicalvibes.model.effect;

/**
 * Removes the permanent named by a {@link PermanentReference} from combat without targeting it.
 * The reference is resolved when the effect resolves, so a missing permanent is a no-op.
 *
 * @param unblockSoleBlockedAttackers when true, attackers the removed creature was blocking that had
 *                                    become blocked by only that creature this combat become unblocked
 *                                    (Imprison)
 */
public record RemoveReferencedPermanentFromCombatEffect(PermanentReference reference,
                                                        boolean unblockSoleBlockedAttackers) implements CardEffect {

    public RemoveReferencedPermanentFromCombatEffect(PermanentReference reference) {
        this(reference, false);
    }
}
