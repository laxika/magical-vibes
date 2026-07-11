package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger descriptor for {@code ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY}: whenever the controller
 * activates an activated ability that isn't a mana ability, they may pay {@code manaCost} to copy
 * that ability. The copy's controller may choose new targets for the copy.
 * <p>
 * At trigger time, {@code TriggerCollectionService.checkControllerActivatesNonManaAbilityTriggers}
 * snapshots the activated ability on the stack and places a {@link MayPayManaEffect} wrapping a
 * {@link CopyControllerActivatedAbilityEffect} on the stack.
 * <p>
 * Used by Rings of Brighthearth.
 *
 * @param manaCost the cost the controller may pay to create the copy (e.g. {@code "{2}"})
 */
public record CopyControllerActivatedAbilityTriggerEffect(
        String manaCost
) implements CardEffect {
}
