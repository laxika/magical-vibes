package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

/**
 * Static effect that replaces the source's creature subtypes with those of its imprinted
 * creature card, while retaining the supplied creature subtypes.
 */
public record SetCreatureTypesToImprintedCreatureEffect(List<CardSubtype> retainedSubtypes)
        implements CardEffect {

    public SetCreatureTypesToImprintedCreatureEffect(CardSubtype retainedSubtype) {
        this(List.of(retainedSubtype));
    }

    public SetCreatureTypesToImprintedCreatureEffect {
        retainedSubtypes = retainedSubtypes == null ? List.of() : List.copyOf(retainedSubtypes);
    }
}
