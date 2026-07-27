package com.github.laxika.magicalvibes.carddata;

import java.util.List;

/**
 * One printed face as the upstream source describes it: provider-neutral, self-contained, and
 * uninterpreted.
 *
 * <p>Deliberately stringly-typed. A loader's job ends at producing one of these — resolving which
 * JSON node a field lives on, provider field naming, array ordering, and syntax quirks. Every
 * *interpretation* of the values (integer parsing, colour symbols, the type line, keyword mapping,
 * text normalisation) happens once in {@link FaceOracleMapper}, so a rules decision cannot drift
 * between providers. The cost is one allocation per face on a startup-only path.
 *
 * <p>Self-contained "by construction" is the load-bearing part. Scryfall ships one object per card
 * with face fields sometimes top-level and sometimes under {@code card_faces[i]}, and MTGJSON ships
 * a separate entry per face; both flatten to this, so the mapper never needs a second node to
 * consult.
 *
 * @param colorIndicator the face's colour indicator, which overrides {@link #colors} on a back face
 *                       that has one — a transformed permanent's colour is printed as an indicator
 *                       rather than derived from a mana cost it does not have
 * @param colorIdentity  the whole card's colour identity, not the face's, and never a rules
 *                       characteristic — it survives only to tint a land's frame in the UI, since a
 *                       land is colourless by {@link #colors}. On Scryfall this field exists only on
 *                       the top-level card, so an extractor must reach up for it when building a
 *                       back face.
 * @param keywords       raw upstream spellings, never mapped {@link com.github.laxika.magicalvibes.model.Keyword}
 *                       values. Text capitalisation matches against these strings and must see
 *                       spellings the enum does not cover ("Ward {2}", "Protection from red"), so
 *                       mapping to the enum first would silently stop capitalising those lines.
 *                       For a double-faced card both providers report the two faces' keywords
 *                       combined, so this is <em>not</em> necessarily only this face's own.
 */
public record RawFace(
        String name,
        String manaCost,
        String typeLine,
        String text,
        List<String> colors,
        List<String> colorIndicator,
        List<String> colorIdentity,
        String power,
        String toughness,
        String loyalty,
        String defense,
        List<String> keywords,
        String watermark) {
}
