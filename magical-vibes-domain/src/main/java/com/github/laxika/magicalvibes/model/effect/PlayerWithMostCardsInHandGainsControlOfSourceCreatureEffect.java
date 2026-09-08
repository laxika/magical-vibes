package com.github.laxika.magicalvibes.model.effect;

/** The unique player with the most cards in hand gains permanent control of the source creature. */
public record PlayerWithMostCardsInHandGainsControlOfSourceCreatureEffect() implements ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.PERMANENT;
    }
}
