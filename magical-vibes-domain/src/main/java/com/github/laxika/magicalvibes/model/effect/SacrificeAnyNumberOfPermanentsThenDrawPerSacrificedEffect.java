package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Sacrifice any number of [permanents matching {@code filter}] you control. Draw a card for each
 * permanent sacrificed this way." (Reprocess.)
 *
 * <p>The controller chooses which (and how many) of their matching permanents to sacrifice via a
 * {@link com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext.SacrificePermanentsDrawPerSacrificed}
 * multi-permanent choice (0 to all); after they are sacrificed the controller draws a card for each
 * one actually sacrificed. Reprocess uses an artifact/creature/land filter.
 *
 * @param filter which permanents the controller may sacrifice
 */
public record SacrificeAnyNumberOfPermanentsThenDrawPerSacrificedEffect(PermanentPredicate filter)
        implements CardEffect {
}
