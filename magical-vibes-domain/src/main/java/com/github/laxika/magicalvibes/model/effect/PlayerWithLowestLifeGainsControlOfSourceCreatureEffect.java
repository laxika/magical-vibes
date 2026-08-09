package com.github.laxika.magicalvibes.model.effect;

/**
 * The player with the lowest life gains control of the source creature. If multiple players are
 * tied for the lowest life, the source's controller chooses one of them.
 */
public record PlayerWithLowestLifeGainsControlOfSourceCreatureEffect() implements ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.PERMANENT;
    }
}
