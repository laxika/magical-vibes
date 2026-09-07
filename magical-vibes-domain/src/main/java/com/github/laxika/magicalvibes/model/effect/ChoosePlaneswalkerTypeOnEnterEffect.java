package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

/** Marker effect for an "as this enters, choose a planeswalker type" choice. */
public record ChoosePlaneswalkerTypeOnEnterEffect() implements SubtypeChoiceOnEnterEffect {

    @Override
    public List<CardSubtype> allowedSubtypes() {
        return CardSubtype.planeswalkerTypes();
    }

    @Override
    public String choicePrompt() {
        return "Choose a planeswalker type.";
    }
}
