package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the source card from its graveyard and creates the follow-up reflexive trigger only if
 * the exile succeeds.
 *
 * <p>The payload deliberately does not contribute its targets to this effect. The payload's
 * targets are chosen after the optional exile is accepted and completed.</p>
 *
 * @param thenEffect the payload of the reflexive trigger
 */
public record ExileSourceCardFromGraveyardThenEffect(CardEffect thenEffect) implements CardEffect {

    public ExileSourceCardFromGraveyardThenEffect {
        if (thenEffect == null) {
            throw new IllegalArgumentException("ExileSourceCardFromGraveyardThenEffect requires a payload");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
