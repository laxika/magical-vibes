package com.github.laxika.magicalvibes.model.effect;

/**
 * For each creature you control, you may search your library for a creature card with the same name
 * as that creature. Put those cards onto the battlefield, then shuffle.
 *
 * <p>Unlike {@link ChooseFivePermanentsSearchSameNameToBattlefieldTappedEffect} nothing is chosen:
 * the names are taken from every creature the controller controls at resolution (one queue entry per
 * creature, so two copies of the same creature grant two searches). Each name becomes one optional
 * single-name library search restricted to creature cards, putting a found card onto the battlefield
 * untapped. Used by Doubling Chant.
 */
public record SearchLibraryForSameNameCreatureForEachControlledCreatureEffect() implements CardEffect {
}
