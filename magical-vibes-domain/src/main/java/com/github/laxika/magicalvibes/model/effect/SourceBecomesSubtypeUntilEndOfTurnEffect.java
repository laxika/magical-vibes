package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * One-shot effect: the <em>source</em> permanent (no target) <em>becomes</em> the given creature type
 * until end of turn, replacing all of its other creature types (e.g. Paragon of the Amesha's activated
 * ability: "this creature becomes an Angel"). Sets {@code Permanent.transientCreatureTypeOverride},
 * which the layered pass reads to strip every creature subtype and add this one. Cleared at end of turn
 * by {@code resetModifiers()}. Self analog of {@link TargetCreatureBecomesSubtypeUntilEndOfTurnEffect}.
 *
 * @param subtype the creature type the source becomes
 */
public record SourceBecomesSubtypeUntilEndOfTurnEffect(CardSubtype subtype) implements CardEffect {
}
