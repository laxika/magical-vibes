package com.github.laxika.magicalvibes.model.filter;

import java.util.List;

/**
 * Factories for the target restrictions cards ask for over and over ("target creature",
 * "target land you control"). Each returns exactly the filter its name describes, including
 * the validation message shown when an illegal target is chosen.
 *
 * <p>These cover the common phrasings only. A card whose oracle text needs a different
 * message — "First target must be a creature", "Second target must be a creature you don't
 * control" — or a restriction with no factory here should build the filter directly; the
 * message is user-facing, so do not reuse a factory whose wording does not match the card.
 */
public final class TargetFilters {

    private TargetFilters() {
    }

    /** "Target must be a creature" */
    public static PermanentPredicateTargetFilter creature() {
        return new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(), "Target must be a creature");
    }

    /** "Target must be a creature you control" */
    public static ControlledPermanentPredicateTargetFilter creatureYouControl() {
        return new ControlledPermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(), "Target must be a creature you control");
    }

    /** "Target must be a creature an opponent controls" */
    public static PermanentPredicateTargetFilter creatureAnOpponentControls() {
        return new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                "Target must be a creature an opponent controls");
    }

    /** "Target must be an attacking creature" */
    public static PermanentPredicateTargetFilter attackingCreature() {
        return new PermanentPredicateTargetFilter(
                new PermanentIsAttackingPredicate(), "Target must be an attacking creature");
    }

    /** "Target must be an unblocked attacking creature" */
    public static PermanentPredicateTargetFilter unblockedAttackingCreature() {
        return new PermanentPredicateTargetFilter(
                new PermanentIsUnblockedAttackingPredicate(),
                "Target must be an unblocked attacking creature");
    }

    /** "Target must be a land" */
    public static PermanentPredicateTargetFilter land() {
        return new PermanentPredicateTargetFilter(
                new PermanentIsLandPredicate(), "Target must be a land");
    }

    /** "Target must be a land you control" */
    public static ControlledPermanentPredicateTargetFilter landYouControl() {
        return new ControlledPermanentPredicateTargetFilter(
                new PermanentIsLandPredicate(), "Target must be a land you control");
    }

    /** "Target must be a land an opponent controls" */
    public static PermanentPredicateTargetFilter landAnOpponentControls() {
        return new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                "Target must be a land an opponent controls");
    }

    /** "Target must be an artifact" */
    public static PermanentPredicateTargetFilter artifact() {
        return new PermanentPredicateTargetFilter(
                new PermanentIsArtifactPredicate(), "Target must be an artifact");
    }

    /** "Target must be an enchantment" */
    public static PermanentPredicateTargetFilter enchantment() {
        return new PermanentPredicateTargetFilter(
                new PermanentIsEnchantmentPredicate(), "Target must be an enchantment");
    }

    /** "Target must be a permanent" — any permanent at all. */
    public static PermanentPredicateTargetFilter permanent() {
        return new PermanentPredicateTargetFilter(
                new PermanentTruePredicate(), "Target must be a permanent");
    }

    /** "Target must be a nonland permanent" */
    public static PermanentPredicateTargetFilter nonlandPermanent() {
        return new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                "Target must be a nonland permanent");
    }

    /** "Target must be a nonland permanent an opponent controls" */
    public static PermanentPredicateTargetFilter nonlandPermanentAnOpponentControls() {
        return new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                "Target must be a nonland permanent an opponent controls");
    }

    /** "Target must be a noncreature permanent an opponent controls" */
    public static PermanentPredicateTargetFilter noncreaturePermanentAnOpponentControls() {
        return new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentIsCreaturePredicate()),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                "Target must be a noncreature permanent an opponent controls");
    }

    /** "Target must be a creature an opponent controls", or a nonland permanent an opponent controls with Gift. */
    public static PermanentPredicateTargetFilter withGift(PermanentPredicateTargetFilter withoutGift,
                                                           PermanentPredicateTargetFilter withGift) {
        return new PermanentPredicateTargetFilter(
                withoutGift.predicate(), withoutGift.errorMessage(), withoutGift.kickedPredicate(),
                withGift.predicate(), withGift.errorMessage());
    }

    /** "Target must be a permanent you control" */
    public static ControlledPermanentPredicateTargetFilter permanentYouControl() {
        return new ControlledPermanentPredicateTargetFilter(
                new PermanentTruePredicate(), "Target must be a permanent you control");
    }
}
