package com.github.laxika.magicalvibes.model.effect;

/**
 * "An opponent may gain control of a creature you control of their choice for as long as
 * [source] remains on the battlefield." Non-targeting resolution choice: an opponent of the
 * ability's controller is offered a may-prompt; on accept they pick one creature that controller
 * controls (auto-takes when only one) and gain control for {@link #duration}. Used after a
 * sacrifice step's "if you can't" (Infernal Denizen).
 */
public record OpponentMayGainControlOfCreatureYouControlEffect(Duration duration)
        implements ControlStealingEffect {

    @Override
    public Duration controlDuration() {
        return duration;
    }
}
