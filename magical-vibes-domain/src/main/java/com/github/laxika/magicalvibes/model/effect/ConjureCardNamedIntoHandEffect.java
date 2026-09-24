package com.github.laxika.magicalvibes.model.effect;

/** Creates a card with the named card's characteristics in the controller's hand. */
public record ConjureCardNamedIntoHandEffect(
        String cardName, boolean discardAtNextEndStep, int repeatUntilHandSize)
        implements CardEffect {

    public ConjureCardNamedIntoHandEffect(String cardName, boolean discardAtNextEndStep) {
        this(cardName, discardAtNextEndStep, 0);
    }

    public ConjureCardNamedIntoHandEffect {
        if (repeatUntilHandSize < 0) {
            throw new IllegalArgumentException("repeatUntilHandSize cannot be negative");
        }
    }
}
