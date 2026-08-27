package com.github.laxika.magicalvibes.model.effect;

/** A modal choice that is made while a triggered ability is being put on the stack. */
public interface TriggeredModalEffect extends CardEffect {

    ChooseOneEffect choice();
}
