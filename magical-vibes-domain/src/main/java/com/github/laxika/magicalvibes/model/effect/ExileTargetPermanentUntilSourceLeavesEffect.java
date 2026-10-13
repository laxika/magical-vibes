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
 * @param exileIfSourceAlreadyLeft whether the target is still exiled if the source left before
 *                                 this effect resolved; no later return is registered in that case
 * @param returnsOnSourceLeavesTrigger whether the return happens through the source's
 *                                     leaves-the-battlefield triggered ability (Faceless Devourer)
 *                                     rather than immediately when the source leaves
 */
public record ExileTargetPermanentUntilSourceLeavesEffect(boolean imprint,
                                                          PermanentPredicate targetPredicate,
                                                          boolean exileIfSourceAlreadyLeft,
                                                          boolean returnsOnSourceLeavesTrigger)
        implements CardEffect {

    public ExileTargetPermanentUntilSourceLeavesEffect(boolean imprint, PermanentPredicate targetPredicate,
                                                       boolean exileIfSourceAlreadyLeft) {
        this(imprint, targetPredicate, exileIfSourceAlreadyLeft, false);
    }

    /** Default constructor — no imprint. */
    public ExileTargetPermanentUntilSourceLeavesEffect() {
        this(false, null, false, false);
    }

    public ExileTargetPermanentUntilSourceLeavesEffect(boolean imprint) {
        this(imprint, null, false);
    }

    public ExileTargetPermanentUntilSourceLeavesEffect(
            boolean imprint, PermanentPredicate targetPredicate) {
        this(imprint, targetPredicate, false);
    }

    /** Models separate enter and leave triggers, where the enter trigger can resolve second. */
    public static ExileTargetPermanentUntilSourceLeavesEffect evenIfSourceAlreadyLeft() {
        return new ExileTargetPermanentUntilSourceLeavesEffect(false, null, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetPredicate == null
                ? TargetSpec.harmful(TargetPredicates.permanent())
                : TargetSpec.harmful(TargetPredicates.permanent(), targetPredicate);
    }
}
