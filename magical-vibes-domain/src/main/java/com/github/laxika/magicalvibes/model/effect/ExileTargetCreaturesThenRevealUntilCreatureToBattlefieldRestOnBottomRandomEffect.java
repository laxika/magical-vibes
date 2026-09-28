package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles each targeted creature, then has each creature's controller reveal cards from their
 * library until a creature card is found. The found card enters that player's battlefield and the
 * other revealed cards are put on the bottom of that library in a random order.
 */
public record ExileTargetCreaturesThenRevealUntilCreatureToBattlefieldRestOnBottomRandomEffect()
        implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
