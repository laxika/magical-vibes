package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Additional/activation cost: sacrifice one permanent matching {@code filter}.
 *
 * @param filter                the permanents that may be sacrificed
 * @param description           human-readable cost description used in prompts
 * @param excludeSource         when true, the ability's own source cannot be sacrificed
 * @param trackSacrificedPower  when true, the sacrificed permanent's effective power is
 *                              snapshotted into the ability's xValue at payment, so a
 *                              companion effect can scale off it via {@code XValue}
 *                              (Freyalise Supplicant)
 * @param trackSacrificedManaValue when true, the sacrificed permanent's mana value is
 *                              snapshotted into the ability's xValue at payment
 *                              (Soldevi Adnate)
 */
public record SacrificePermanentCost(PermanentPredicate filter, String description, boolean excludeSource,
                                     boolean trackSacrificedPower,
                                     boolean trackSacrificedManaValue) implements CostEffect {
    public SacrificePermanentCost(PermanentPredicate filter, String description) {
        this(filter, description, true, false, false);
    }

    public SacrificePermanentCost(PermanentPredicate filter, String description, boolean excludeSource) {
        this(filter, description, excludeSource, false, false);
    }

    public SacrificePermanentCost(PermanentPredicate filter, String description, boolean excludeSource,
                                  boolean trackSacrificedPower) {
        this(filter, description, excludeSource, trackSacrificedPower, false);
    }

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return filter;
    }
}
