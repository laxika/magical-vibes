package com.github.laxika.magicalvibes.model.effect;

/**
 * Scrambleverse: "For each nonland permanent, choose a player at random. Then each player gains
 * control of each permanent for which they were chosen. Untap those permanents."
 *
 * <p>Non-targeting. Every nonland permanent on the battlefield gets an independent random player
 * assignment; a permanent whose randomly chosen player already controls it simply stays put, but is
 * still untapped.</p>
 */
public record ScrambleverseEffect() implements CardEffect, ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.PERMANENT;
    }
}
