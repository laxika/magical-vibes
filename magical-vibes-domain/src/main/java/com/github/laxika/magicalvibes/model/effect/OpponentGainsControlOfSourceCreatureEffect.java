package com.github.laxika.magicalvibes.model.effect;

/**
 * "An opponent gains control of this creature." Mandatory (unlike
 * {@link OpponentMayGainControlOfCreatureYouControlEffect}, which offers a choice and lets the
 * opponent pick which creature) and always about the source permanent itself — Rogue Skycaptain's
 * unpaid-wage penalty. The ability's controller chooses the opponent as it resolves (the one opponent, unasked).
 *
 * @param duration how long the control change lasts
 * @param opponent the opponent who gains control, or {@code null} for the one the controller chooses as it resolves
 */
public record OpponentGainsControlOfSourceCreatureEffect(Duration duration, PlayerSubject opponent)
        implements ControlStealingEffect {

    public OpponentGainsControlOfSourceCreatureEffect(Duration duration) {
        this(duration, null);
    }

    @Override
    public Duration controlDuration() {
        return duration;
    }
}
