package com.github.laxika.magicalvibes.model.effect;

/**
 * "An opponent gains control of this creature." Mandatory (unlike
 * {@link OpponentMayGainControlOfCreatureYouControlEffect}, which offers a choice and lets the
 * opponent pick which creature) and always about the source permanent itself — Rogue Skycaptain's
 * unpaid-wage penalty. In a two-player game the sole opponent of the ability's controller gains
 * control; with more opponents the first one in turn order does.
 *
 * @param duration how long the control change lasts
 */
public record OpponentGainsControlOfSourceCreatureEffect(ControlDuration duration)
        implements ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return duration;
    }
}
