package com.github.laxika.magicalvibes.model.effect;

/** Increases the persistent intensity of cards owned by the controller with the given name. */
public record IntensifyNamedCardsEffect(String cardName, int amount) implements CardEffect {

    public IntensifyNamedCardsEffect(String cardName) {
        this(cardName, 1);
    }
}
