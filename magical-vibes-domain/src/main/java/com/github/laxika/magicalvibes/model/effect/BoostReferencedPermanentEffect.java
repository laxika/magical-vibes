package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * The permanent named by a {@link PermanentReference} gets +X/+Y until end of turn.
 * This is the non-targeting counterpart to {@link BoostTargetCreatureEffect}; it is used when
 * the permanent is fixed by the event that created a triggered ability.
 */
public record BoostReferencedPermanentEffect(PermanentReference reference,
                                             DynamicAmount powerBoost,
                                             DynamicAmount toughnessBoost) implements CardEffect {

    public BoostReferencedPermanentEffect(PermanentReference reference, int powerBoost, int toughnessBoost) {
        this(reference, new Fixed(powerBoost), new Fixed(toughnessBoost));
    }
}
