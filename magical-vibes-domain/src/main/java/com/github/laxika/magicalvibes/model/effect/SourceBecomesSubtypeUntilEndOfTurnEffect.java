package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;
import java.util.Objects;

/**
 * One-shot effect: the <em>source</em> permanent (no target) <em>becomes</em> the given creature type(s)
 * until end of turn, replacing all of its other creature types (e.g. Paragon of the Amesha's activated
 * ability: "this creature becomes an Angel"). Sets the permanent's transient creature type override,
 * which the layered pass reads to strip every creature subtype and add these. Cleared at end of turn by
 * {@code resetModifiers()}. Self analog of {@link TargetCreatureBecomesSubtypeUntilEndOfTurnEffect}.
 *
 * @param subtypes the creature types the source becomes
 */
public record SourceBecomesSubtypeUntilEndOfTurnEffect(List<CardSubtype> subtypes) implements CardEffect {

    public SourceBecomesSubtypeUntilEndOfTurnEffect {
        Objects.requireNonNull(subtypes, "subtypes");
        if (subtypes.isEmpty()) {
            throw new IllegalArgumentException("subtypes must not be empty");
        }
        subtypes = List.copyOf(subtypes);
    }

    public SourceBecomesSubtypeUntilEndOfTurnEffect(CardSubtype subtype) {
        this(List.of(Objects.requireNonNull(subtype, "subtype")));
    }

    public CardSubtype subtype() {
        return subtypes.getFirst();
    }
}
