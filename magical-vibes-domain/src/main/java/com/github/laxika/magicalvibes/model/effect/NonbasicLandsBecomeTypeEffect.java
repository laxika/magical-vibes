package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Global static effect: every nonbasic land (regardless of controller) becomes the given basic
 * land type. "Nonbasic lands are [type]s."
 *
 * <p>Per MTG rule 305.7, an affected land loses its other land types and abilities, gaining only
 * the intrinsic mana ability of the new basic land type. Basic lands are unaffected.
 *
 * <p>Reusable for Blood Moon / Magus of the Moon ("Nonbasic lands are Mountains").
 *
 * @param subtype the basic land type each nonbasic land becomes (e.g. MOUNTAIN)
 */
public record NonbasicLandsBecomeTypeEffect(CardSubtype subtype) implements CardEffect {
}
