package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

/** During resolution, secretly choose a subtype and store it on the source permanent. */
public record ChooseSubtypeForSourceEffect(List<CardSubtype> allowedSubtypes,
                                           boolean untilEndOfTurn,
                                           String choicePrompt)
        implements SubtypeChoiceOnEnterEffect {

    public ChooseSubtypeForSourceEffect() {
        this(List.of(), false, "Choose a creature type.");
    }

    public ChooseSubtypeForSourceEffect(List<CardSubtype> allowedSubtypes) {
        this(allowedSubtypes, false, "Choose a creature type.");
    }

    public ChooseSubtypeForSourceEffect {
        allowedSubtypes = allowedSubtypes == null ? List.of() : List.copyOf(allowedSubtypes);
        choicePrompt = choicePrompt == null ? "Choose a creature type." : choicePrompt;
    }
}
