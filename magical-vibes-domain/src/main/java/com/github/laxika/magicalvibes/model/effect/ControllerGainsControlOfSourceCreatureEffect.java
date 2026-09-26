package com.github.laxika.magicalvibes.model.effect;

/**
 * Gives control of the source creature to the player who activated or controls the stack entry.
 * Unlike {@link OpponentGainsControlOfSourceCreatureEffect}, this is the activator/entry
 * controller itself, which matters for abilities that opponents may activate.
 *
 * @param duration how long control of the source creature is retained
 */
public record ControllerGainsControlOfSourceCreatureEffect(ControlDuration duration)
        implements ControlStealingEffect {

    public ControllerGainsControlOfSourceCreatureEffect() {
        this(ControlDuration.PERMANENT);
    }

    @Override
    public ControlDuration controlDuration() {
        return duration;
    }
}
