package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

/** Marks the creature-type choice made by Champion of the Hareish's buddy list ability. */
public record BuddyListOnEnterEffect(List<CardSubtype> allowedSubtypes)
        implements SubtypeChoiceOnEnterEffect {

    public BuddyListOnEnterEffect {
        allowedSubtypes = allowedSubtypes == null ? List.of() : List.copyOf(allowedSubtypes);
    }

    @Override
    public boolean writesToBuddyList() {
        return true;
    }

    @Override
    public String choicePrompt() {
        return "Choose a creature type for your buddy list.";
    }
}
