package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Global static effect: while the source is on the battlefield, activated abilities with {T} in
 * their costs of permanents matching {@code affectedPredicate} can't be activated (non-tap
 * activated abilities stay usable). Board-wide sibling of
 * {@link EnchantedCreatureCantActivateTapAbilitiesEffect} (Serra Bestiary); contrast
 * {@link ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect} which locks every activated
 * ability. Enforced in {@code AbilityActivationService} on the mana-tap and {@code isRequiresTap()}
 * paths, and in {@code GameQueryService.canActivateManaAbility}. Used by Katabatic Winds
 * ("Creatures with flying … their activated abilities with {T} in their costs can't be activated").
 *
 * @param affectedPredicate which permanents have their {T} abilities locked
 */
public record MatchingPermanentsCantActivateTapAbilitiesEffect(PermanentPredicate affectedPredicate)
        implements CardEffect {
}
