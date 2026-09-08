package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that prevents permanents entering the battlefield from causing triggered
 * abilities of permanents controlled by this effect's controller's opponents to trigger.
 */
public record OpponentPermanentsEnteringDontCauseTriggersEffect() implements CardEffect {
}
