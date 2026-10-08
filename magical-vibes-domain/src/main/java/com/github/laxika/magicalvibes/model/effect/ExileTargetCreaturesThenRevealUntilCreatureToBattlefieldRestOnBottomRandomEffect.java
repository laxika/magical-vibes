package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Exiles each targeted creature, then has each creature's controller reveal cards from their
 * library until a creature card is found. The found card enters that player's battlefield and the
 * other revealed cards are put on the bottom of that library in a random order.
 */
public record ExileTargetCreaturesThenRevealUntilCreatureToBattlefieldRestOnBottomRandomEffect(
        List<UUID> controllersToReveal)
        implements RemovalEffect {

    public ExileTargetCreaturesThenRevealUntilCreatureToBattlefieldRestOnBottomRandomEffect() {
        this(null);
    }

    public ExileTargetCreaturesThenRevealUntilCreatureToBattlefieldRestOnBottomRandomEffect {
        if (controllersToReveal != null) {
            controllersToReveal = List.copyOf(controllersToReveal);
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return controllersToReveal == null ? TargetSpec.harmful(TargetPredicates.creature()) : TargetSpec.NONE;
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
