package com.github.laxika.magicalvibes.model.effect;

/**
 * "Conjure a card named … into your hand [. Discard that card at the beginning of the next end step]": creates the
 * card of the given printing in the controller's hand. Nothing is conjured while that printing is not implemented.
 * "If you have fewer than N cards in hand, repeat this process" is
 * {@code ConjureCardsEffect.named(...).repeatingWhileHandSmallerThan(N)}.
 */
public record ConjureCardNamedIntoHandEffect(String setCode, String collectorNumber, boolean discardAtNextEndStep)
        implements CardEffect {
}
