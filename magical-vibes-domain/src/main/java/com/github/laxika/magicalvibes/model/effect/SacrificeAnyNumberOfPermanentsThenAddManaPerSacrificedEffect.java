package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Sacrifice any number of [permanents matching {@code filter}], then add that much [{@code color}]."
 * (Mana Seism.)
 *
 * <p>The controller chooses which (and how many) of their matching permanents to sacrifice via a
 * {@link com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext.SacrificePermanentsAddManaPerSacrificed}
 * multi-permanent choice (0 to all); afterwards one mana of {@code color} is added to their pool for
 * each permanent actually sacrificed. The mana sibling of
 * {@link SacrificeAnyNumberOfPermanentsThenDrawPerSacrificedEffect}.
 *
 * <p>This is a spell effect, not a mana ability, so it deliberately does not implement
 * {@link ManaProducingEffect} (which marks mana abilities per CR 605.1a).
 *
 * @param filter which permanents the controller may sacrifice
 * @param color  the color of mana added per permanent sacrificed
 */
public record SacrificeAnyNumberOfPermanentsThenAddManaPerSacrificedEffect(
        PermanentPredicate filter, ManaColor color) implements CardEffect {
}
