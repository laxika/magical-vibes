package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the targeted permanents, then has each player reveal cards until a permanent card is
 * found and put that card onto the battlefield under their control. The other revealed cards are
 * put on the bottom of their library in a random order.
 */
public record ExileTargetPermanentsThenEachPlayerRevealsUntilPermanentEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
