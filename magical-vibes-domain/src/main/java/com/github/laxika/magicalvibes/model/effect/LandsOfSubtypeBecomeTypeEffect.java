package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Global static effect: every land carrying {@code fromSubtype} (regardless of controller, basic or
 * nonbasic) becomes the basic land type {@code toSubtype}. "All [from]s are [to]s."
 *
 * <p>Per MTG rule 305.7, an affected land loses its other land types and abilities, gaining only the
 * intrinsic mana ability of the new basic land type. Lands without {@code fromSubtype} are
 * unaffected.
 *
 * <p>Contrast {@link NonbasicLandsBecomeTypeEffect} (Blood Moon), which rewrites <em>nonbasic</em>
 * lands unconditionally. This one keys off a land subtype and also affects basic lands.
 *
 * <p>Reusable for Conversion ("All Mountains are Plains").
 *
 * @param fromSubtype the land subtype an affected land must carry (e.g. MOUNTAIN)
 * @param toSubtype   the basic land type each affected land becomes (e.g. PLAINS)
 */
public record LandsOfSubtypeBecomeTypeEffect(CardSubtype fromSubtype, CardSubtype toSubtype)
        implements CardEffect {
}
