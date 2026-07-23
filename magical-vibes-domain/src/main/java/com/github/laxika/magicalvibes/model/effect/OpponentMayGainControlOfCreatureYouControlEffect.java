package com.github.laxika.magicalvibes.model.effect;

/**
 * "An opponent may gain control of a creature you control of their choice for as long as
 * [source] remains on the battlefield." Non-targeting resolution choice: an opponent of the
 * ability's controller is offered a may-prompt; on accept they pick one creature that controller
 * controls (auto-takes when only one) and gain control for {@link #duration}. Used as a
 * {@link ForcedCostOrElseEffect} fallback (Infernal Denizen).
 */
public record OpponentMayGainControlOfCreatureYouControlEffect(ControlDuration duration)
        implements ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return duration;
    }
}
