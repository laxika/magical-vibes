package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles each chosen permanent, then has each permanent's controller reveal cards from their
 * library until they reveal a permanent card sharing a card type with that permanent. Each found
 * card is put onto the battlefield under its controller's control and the other revealed cards are
 * shuffled into that library.
 */
public record ExileEachTargetPermanentThenRevealUntilSharedCardTypeEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
