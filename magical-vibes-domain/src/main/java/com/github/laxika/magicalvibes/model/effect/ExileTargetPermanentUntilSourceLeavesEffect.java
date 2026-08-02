package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Exile target permanent until the source permanent leaves the battlefield.
 * When the source leaves (by any means — death, bounce, exile), the exiled card
 * returns to the battlefield under its owner's control.
 * Used by O-ring style creatures like Leonin Relic-Warder.
 *
 * @param imprint if true, also imprints the exiled card onto the source permanent
 *                (e.g. Ixalan's Binding uses imprint for its "can't cast same name" static)
 * @param targetPredicate optional restriction on the permanent target
 */
public record ExileTargetPermanentUntilSourceLeavesEffect(boolean imprint,
                                                          PermanentPredicate targetPredicate)
        implements CardEffect {

    /** Default constructor — no imprint. */
    public ExileTargetPermanentUntilSourceLeavesEffect() {
        this(false, null);
    }

    public ExileTargetPermanentUntilSourceLeavesEffect(boolean imprint) {
        this(imprint, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetPredicate == null
                ? TargetSpec.harmful(TargetCategory.PERMANENT)
                : TargetSpec.harmful(TargetCategory.PERMANENT, targetPredicate);
    }
}
