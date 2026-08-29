package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Additional/activation cost: sacrifice one permanent matching {@code filter}.
 *
 * @param filter                   the permanents that may be sacrificed
 * @param description              human-readable cost description used in prompts
 * @param excludeSource            when true, the ability's own source cannot be sacrificed
 * @param trackSacrificedPower     when true, the sacrificed permanent's effective power is
 *                                 snapshotted into the ability's xValue at payment, so a
 *                                 companion effect can scale off it via {@code XValue}
 *                                 (Freyalise Supplicant)
 * @param trackSacrificedManaValue when true, the sacrificed permanent's mana value is
 *                                 snapshotted into the ability's xValue at payment
 *                                 (Soldevi Adnate)
 * @param trackSacrificedToughness when true, the sacrificed permanent's effective toughness is
 *                                 snapshotted into the ability's xValue at payment
 *                                 (Korozda Guildmage)
 */
public record SacrificePermanentCost(PermanentPredicate filter, String description, boolean excludeSource,
                                     boolean trackSacrificedPower,
                                     boolean trackSacrificedManaValue,
                                     boolean trackSacrificedToughness) implements CostEffect {
    public SacrificePermanentCost(PermanentPredicate filter, String description) {
        this(filter, description, true, false, false, false);
    }

    public SacrificePermanentCost(PermanentPredicate filter, String description, boolean excludeSource) {
        this(filter, description, excludeSource, false, false, false);
    }

    public SacrificePermanentCost(PermanentPredicate filter, String description, boolean excludeSource,
                                  boolean trackSacrificedPower) {
        this(filter, description, excludeSource, trackSacrificedPower, false, false);
    }

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return filter;
    }

    @Override
    public boolean sacrificesChosenPermanent() {
        return true;
    }
}
