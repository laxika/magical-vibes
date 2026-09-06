package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

/**
 * Marker effect placed in ON_ENTER_BATTLEFIELD to indicate that this permanent
 * requires a creature type choice as it enters the battlefield ("As ~ enters, choose a creature type.").
 */
public record ChooseSubtypeOnEnterEffect(List<CardSubtype> allowedSubtypes, boolean opponentChooses)
        implements SubtypeChoiceOnEnterEffect {

    public ChooseSubtypeOnEnterEffect() {
        this(List.of(), false);
    }

    public ChooseSubtypeOnEnterEffect(List<CardSubtype> allowedSubtypes) {
        this(allowedSubtypes, false);
    }

    public ChooseSubtypeOnEnterEffect(boolean opponentChooses) {
        this(List.of(), opponentChooses);
    }

    public ChooseSubtypeOnEnterEffect {
        allowedSubtypes = allowedSubtypes == null ? List.of() : List.copyOf(allowedSubtypes);
    }
}
