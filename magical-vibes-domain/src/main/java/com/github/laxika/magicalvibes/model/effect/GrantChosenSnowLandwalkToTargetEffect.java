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
 * <p>Only the five basic land types are offered, matching every other land-type choice in the
 * engine ({@code PlayerInputService.beginBasicLandTypeChoice} and siblings). CR 702.14a allows
 * any land type, but no snow land outside the basic types exists in the implemented sets.
 *
 * <p>Used by Barbarian Guides.
 */
public record GrantChosenSnowLandwalkToTargetEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE);
    }
}
