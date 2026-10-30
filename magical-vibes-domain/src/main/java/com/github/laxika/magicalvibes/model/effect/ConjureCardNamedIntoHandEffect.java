package com.github.laxika.magicalvibes.model.effect;

/**
 * "Conjure a card named … into your hand": creates the card of the given printing in the
 * controller's hand. Nothing is conjured while that printing is not implemented.
 */
public record ConjureCardNamedIntoHandEffect(
        String setCode, String collectorNumber, boolean discardAtNextEndStep, int repeatUntilHandSize)
        implements CardEffect {

    public ConjureCardNamedIntoHandEffect(String setCode, String collectorNumber, boolean discardAtNextEndStep) {
        this(setCode, collectorNumber, discardAtNextEndStep, 0);
    }

    public ConjureCardNamedIntoHandEffect {
        if (repeatUntilHandSize < 0) {
            throw new IllegalArgumentException("repeatUntilHandSize cannot be negative");
        }
    }
}
