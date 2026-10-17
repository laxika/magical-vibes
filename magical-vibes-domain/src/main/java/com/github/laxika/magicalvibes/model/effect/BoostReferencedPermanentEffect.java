package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.Set;

/**
 * The permanent named by a {@link PermanentReference} gets +X/+Y until end of turn.
 * This is the non-targeting counterpart to {@link BoostTargetCreatureEffect}; it is used when
 * the permanent is fixed by the event that created a triggered ability or by a preceding targeted
 * graveyard return. The optional {@code keywords} are granted to the same permanent until end of turn
 * (Macabre Mockery: "It gets +2/+0 and gains haste until end of turn").
 */
public record BoostReferencedPermanentEffect(PermanentReference reference,
                                             DynamicAmount powerBoost,
                                             DynamicAmount toughnessBoost,
                                             Set<Keyword> keywords) implements CardEffect {

    public BoostReferencedPermanentEffect {
        keywords = keywords == null ? Set.of() : Set.copyOf(keywords);
    }

    public BoostReferencedPermanentEffect(PermanentReference reference, DynamicAmount powerBoost,
                                          DynamicAmount toughnessBoost) {
        this(reference, powerBoost, toughnessBoost, Set.of());
    }

    public BoostReferencedPermanentEffect(PermanentReference reference, int powerBoost, int toughnessBoost) {
        this(reference, new Fixed(powerBoost), new Fixed(toughnessBoost));
    }
}
