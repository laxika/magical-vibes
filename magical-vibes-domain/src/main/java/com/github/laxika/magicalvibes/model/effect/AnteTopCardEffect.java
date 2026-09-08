package com.github.laxika.magicalvibes.model.effect;

/** Moves one or more players' top library cards into the ante zone. */
public record AnteTopCardEffect(AnteRecipient recipient) implements CardEffect {

    /** Moves the controller's top library card into the ante zone. */
    public AnteTopCardEffect() {
        this(AnteRecipient.CONTROLLER);
    }
}
