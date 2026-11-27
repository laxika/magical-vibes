package com.github.laxika.magicalvibes.model.effect;

/**
 * "An opponent may gain control of a creature you control of their choice for as long as
 * [source] remains on the battlefield." Non-targeting resolution choice: the ability's controller
 * chooses the opponent as it resolves (the one opponent, unasked), who is offered a may-prompt; on
 * accept they pick one creature that controller controls (auto-takes when only one) and gain control
 * for {@link #duration}. Used after a sacrifice step's "if you can't" (Infernal Denizen).
 *
 * @param duration how long the control change lasts
 * @param opponent the opponent offered the creature, or {@code null} for the one the controller chooses as it resolves
 */
public record OpponentMayGainControlOfCreatureYouControlEffect(Duration duration, PlayerSubject opponent)
        implements ControlStealingEffect {

    public OpponentMayGainControlOfCreatureYouControlEffect(Duration duration) {
        this(duration, null);
    }

    @Override
    public Duration controlDuration() {
        return duration;
    }
}
