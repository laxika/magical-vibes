package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;
import java.util.Objects;

/**
 * One-shot effect: the targeted creature <em>becomes</em> the given creature type(s) until end of
 * turn, replacing all of its other creature types (e.g. Boldwyr Intimidator: "{R}: Target creature
 * becomes a Coward until end of turn."). Sets {@code Permanent.transientCreatureTypeOverride(s)},
 * which the layered pass reads to strip every creature subtype and add the new ones. Cleared at end
 * of turn by {@code resetModifiers()}. Contrast {@link GrantSubtypeToTargetCreatureEffect}, which is
 * permanent and additive ("in addition to its other types").
 *
 * @param subtypes the creature types the target becomes
 */
public record TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(List<CardSubtype> subtypes, PermanentSubject subject)
        implements CardEffect {

    public TargetCreatureBecomesSubtypeUntilEndOfTurnEffect {
        Objects.requireNonNull(subtypes, "subtypes");
        if (subtypes.isEmpty()) {
            throw new IllegalArgumentException("subtypes must not be empty");
        }
        subtypes = List.copyOf(subtypes);
    }

    public TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype subtype, PermanentSubject subject) {
        this(List.of(Objects.requireNonNull(subtype, "subtype")), subject);
    }

    public TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype subtype) {
        this(List.of(Objects.requireNonNull(subtype, "subtype")), Subjects.target());
    }

    public TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(List<CardSubtype> subtypes) {
        this(subtypes, Subjects.target());
    }

    public CardSubtype subtype() {
        return subtypes.getFirst();
    }

    @Override
    public TargetSpec targetSpec() {
        if (subject.declaresPlayerTarget()) {
            return TargetSpec.benign(TargetPredicates.player());
        }
        return subject.isSet() ? TargetSpec.NONE : TargetSpec.benign(TargetPredicates.creature());
    }
}
