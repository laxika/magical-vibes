package com.github.laxika.magicalvibes.model.effect;

/**
 * On resolution, prompts the controller to choose a land type, then gives the target creature
 * snow landwalk of that type until end of turn: it can't be blocked as long as the defending
 * player controls a land that is both snow and of the chosen type (CR 702.14c — snow landwalk
 * requires the specified supertype <em>and</em> subtype, so a non-snow land of that type does
 * not turn it on).
 *
 * <p>The grant is stored on the target as an until-end-of-turn defender-condition entry
 * ({@code Permanent.unblockableIfDefenderControlsUntilEndOfTurn}) rather than as a
 * {@link com.github.laxika.magicalvibes.model.Keyword}: the landwalk keywords check the land
 * subtype only, so granting one of them would give plain, non-snow landwalk. The printed,
 * fixed-type form of the same evasion is
 * {@link CantBeBlockedIfDefenderControlsMatchingPermanentEffect} (Legions of Lim-Dûl).
 *
 * <p>Every land type of CR 205.3i is offered, not only the basic ones ("choose a land type", CR 702.14a).
 *
 * <p>Used by Barbarian Guides.
 */
public record GrantChosenSnowLandwalkToTargetEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
