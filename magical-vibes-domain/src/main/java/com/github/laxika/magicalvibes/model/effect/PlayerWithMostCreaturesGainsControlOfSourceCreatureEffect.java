package com.github.laxika.magicalvibes.model.effect;

/** The unique player with the most creatures gains permanent control of the source creature. */
public record PlayerWithMostCreaturesGainsControlOfSourceCreatureEffect() implements ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.PERMANENT;
    }
}
