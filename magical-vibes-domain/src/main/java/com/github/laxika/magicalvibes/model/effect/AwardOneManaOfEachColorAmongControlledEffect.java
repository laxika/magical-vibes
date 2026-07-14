package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "For each color among permanents you control matching {@code predicate}, add one mana of that
 * color." Unlike {@link AwardManaOfColorsAmongControlledEffect} (Mox Amber, which lets the player
 * pick a single color), this adds one mana of every color found simultaneously. Bloom Tender's
 * Vivid ability uses this with a match-all predicate.
 */
public record AwardOneManaOfEachColorAmongControlledEffect(PermanentPredicate predicate) implements ManaProducingEffect {
}
