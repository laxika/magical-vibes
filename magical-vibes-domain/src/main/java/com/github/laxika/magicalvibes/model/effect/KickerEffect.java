package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect declaring that a spell has kicker — an optional additional cost
 * that can be paid when casting. (MTG Rule 702.32)
 *
 * @param cost the mana cost string for the kicker (e.g. "{4}", "{1}{G}")
 */
public record KickerEffect(String cost) implements CardEffect {
}
