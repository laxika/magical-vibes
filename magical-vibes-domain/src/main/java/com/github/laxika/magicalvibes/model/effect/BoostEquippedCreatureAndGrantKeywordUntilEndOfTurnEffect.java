package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

/**
 * The source equipment's equipped creature gets +powerBoost/+toughnessBoost and gains the given
 * keyword until end of turn. If the source equipment is not attached to a creature, the effect
 * does nothing.
 *
 * <p>Used to model equipment-granted triggered abilities such as Diviner's Wand's "Whenever you
 * draw a card, this creature gets +1/+1 and gains flying until end of turn" (placed in the
 * {@code ON_CONTROLLER_DRAWS} slot on the equipment).
 */
public record BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect(
        int powerBoost,
        int toughnessBoost,
        Keyword keyword
) implements CardEffect {
}
