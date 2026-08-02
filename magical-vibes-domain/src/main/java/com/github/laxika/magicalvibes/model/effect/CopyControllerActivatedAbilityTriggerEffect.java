package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

/**
 * Trigger descriptor for {@code ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY}: whenever the controller
 * activates an activated ability that isn't a mana ability, they may pay {@code manaCost} to copy
 * that ability. The copy's controller may choose new targets for the copy.
 * <p>
 * At trigger time, {@code TriggerCollectionService.checkControllerActivatesNonManaAbilityTriggers}
 * snapshots the activated ability on the stack and places a {@link MayPayManaEffect} wrapping a
 * {@link CopyControllerActivatedAbilityEffect} on the stack.
 * <p>
 * Used by Rings of Brighthearth ({@code sourceFilter == null}) and Kurkesh, Onakke Ancient
 * (a {@code StackEntryCardTypeInPredicate(ARTIFACT)} source filter, so only abilities of
 * artifacts trigger it).
 *
 * @param manaCost     the cost the controller may pay to create the copy (e.g. {@code "{2}"})
 * @param sourceFilter optional restriction on the activated ability's stack entry (its card is the
 *                     ability's source); {@code null} means every non-mana ability triggers
 */
public record CopyControllerActivatedAbilityTriggerEffect(
        String manaCost,
        StackEntryPredicate sourceFilter
) implements CardEffect {

    public CopyControllerActivatedAbilityTriggerEffect(String manaCost) {
        this(manaCost, null);
    }
}
