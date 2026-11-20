package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/**
 * Exile target permanent until the source permanent leaves the battlefield.
 * When the source leaves (by any means — death, bounce, exile), the exiled card
 * returns to the battlefield under its owner's control.
 * Used by O-ring style creatures like Leonin Relic-Warder.
 *
 * <p>{@code permanents} names what is exiled: the target, or the target and others the target decides ("exile target
 * nonland permanent and all other permanents with the same name as that permanent", Detention Sphere:
 * {@code allOf(target(), otherPermanentsSharing(target(), NAME, any))}). Only the target is targeted; the others are
 * exiled even with protection or hexproof, and nothing is exiled once the sole target is illegal (CR 608.2b). Each
 * card exiled is linked to the source; several exiled by one leaves-the-battlefield-returning ability return together.
 *
 * @param imprint if true, also imprints the exiled card onto the source permanent
 *                (e.g. Ixalan's Binding uses imprint for its "can't cast same name" static)
 * @param targetPredicate optional restriction on the permanent target
 * @param exileIfSourceAlreadyLeft whether the target is still exiled if the source left before
 *                                 this effect resolved; no later return is registered in that case
 * @param returnsOnSourceLeavesTrigger whether the return happens through the source's
 *                                     leaves-the-battlefield triggered ability (Faceless Devourer)
 *                                     rather than immediately when the source leaves
 * @param controllerMayCastWithAnyMana whether the ability's controller may cast the exiled card for
 *                                     as long as it remains exiled, spending mana of any type
 *                                     (Hostage Taker); the permission belongs to that player, not
 *                                     to whoever controls the source later
 * @param permanents what is exiled; it declares the target ({@code target()} by default)
 */
public record ExileTargetPermanentUntilSourceLeavesEffect(boolean imprint,
                                                          PermanentPredicate targetPredicate,
                                                          boolean exileIfSourceAlreadyLeft,
                                                          boolean returnsOnSourceLeavesTrigger,
                                                          boolean controllerMayCastWithAnyMana,
                                                          PermanentSubject permanents)
        implements CardEffect {

    public ExileTargetPermanentUntilSourceLeavesEffect {
        Objects.requireNonNull(permanents, "permanents");
        if (!permanents.declaresTarget()) {
            throw new IllegalArgumentException("The exile names its target: " + permanents);
        }
    }

    public ExileTargetPermanentUntilSourceLeavesEffect(boolean imprint, PermanentPredicate targetPredicate,
                                                       boolean exileIfSourceAlreadyLeft,
                                                       boolean returnsOnSourceLeavesTrigger,
                                                       boolean controllerMayCastWithAnyMana) {
        this(imprint, targetPredicate, exileIfSourceAlreadyLeft, returnsOnSourceLeavesTrigger,
                controllerMayCastWithAnyMana, Subjects.target());
    }

    /**
     * Exiles {@code permanents}, which name the target matching {@code targetPredicate} and the permanents it decides
     * (Detention Sphere, Deputy of Detention).
     */
    public ExileTargetPermanentUntilSourceLeavesEffect(PermanentSubject permanents, PermanentPredicate targetPredicate,
                                                       boolean exileIfSourceAlreadyLeft,
                                                       boolean returnsOnSourceLeavesTrigger) {
        this(false, targetPredicate, exileIfSourceAlreadyLeft, returnsOnSourceLeavesTrigger, false, permanents);
    }

    public ExileTargetPermanentUntilSourceLeavesEffect(boolean imprint, PermanentPredicate targetPredicate,
                                                       boolean exileIfSourceAlreadyLeft,
                                                       boolean returnsOnSourceLeavesTrigger) {
        this(imprint, targetPredicate, exileIfSourceAlreadyLeft, returnsOnSourceLeavesTrigger, false);
    }

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

    /** Hostage Taker: the ability's controller may cast the exiled card with mana of any type. */
    public static ExileTargetPermanentUntilSourceLeavesEffect castableByControllerWithAnyMana() {
        return new ExileTargetPermanentUntilSourceLeavesEffect(false, null, false, false, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetPredicate == null
                ? TargetSpec.harmful(TargetPredicates.permanent())
                : TargetSpec.harmful(TargetPredicates.permanent(), targetPredicate);
    }
}
