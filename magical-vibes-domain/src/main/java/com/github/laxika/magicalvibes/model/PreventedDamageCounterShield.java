package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.UUID;

/**
 * "Prevent the next N damage that would be dealt to target creature this turn. Put a +0/+1 counter on it for each 1
 * damage prevented this way at the beginning of the next end step" (Sacred Boon, Scars of the Veteran): the prevention
 * (CR 615.7) as the floating effect on the creature carries it. Each damage it prevents is also counted by a
 * {@link PreventedDamageCounterTally} the delayed triggered ability reads.
 *
 * @param shieldId  what the delayed triggered ability names the shield by
 * @param remaining the damage the shield can still prevent
 */
public record PreventedDamageCounterShield(UUID shieldId, int remaining) implements CardEffect {

    /** The same shield after {@code prevented} damage has been prevented. */
    public PreventedDamageCounterShield reducedBy(int prevented) {
        return new PreventedDamageCounterShield(shieldId, remaining - prevented);
    }
}
