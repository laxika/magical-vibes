package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top cards of the targeted player's library and gives the effect controller
 * permission to play those cards until end of turn.
 */
public record ExileTopCardsOfTargetPlayerControllerMayPlayThisTurnEffect(int count)
        implements CardEffect {

    public ExileTopCardsOfTargetPlayerControllerMayPlayThisTurnEffect {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
