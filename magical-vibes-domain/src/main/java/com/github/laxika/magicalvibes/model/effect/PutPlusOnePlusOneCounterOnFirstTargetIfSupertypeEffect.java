package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSupertype;

/**
 * Put one or more +1/+1 counters on the first targeted permanent, but only if
 * it has the specified supertype (e.g. {@link CardSupertype#LEGENDARY}).
 * <p>
 * Designed for multi-target spells like Ancient Animus where the counter
 * placement is conditional on the first target's supertype.
 *
 * @param supertype the supertype the first target must have
 * @param count     number of +1/+1 counters to place
 */
public record PutPlusOnePlusOneCounterOnFirstTargetIfSupertypeEffect(
        CardSupertype supertype,
        int count
) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
