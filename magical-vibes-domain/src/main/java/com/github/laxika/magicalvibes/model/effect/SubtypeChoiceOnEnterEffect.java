package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.List;

/** Describes a subtype choice made while a permanent enters the battlefield. */
public interface SubtypeChoiceOnEnterEffect extends CardEffect {

    List<CardSubtype> allowedSubtypes();

    default String choicePrompt() {
        return "Choose a creature type.";
    }
}
