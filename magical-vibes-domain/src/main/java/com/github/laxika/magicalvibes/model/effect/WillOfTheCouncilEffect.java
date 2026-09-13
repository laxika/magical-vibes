package com.github.laxika.magicalvibes.model.effect;

/**
 * Starting with the effect controller, each player votes for a nonland permanent the effect
 * controller does not control. Permanents tied for the most votes are exiled after all votes are
 * cast.
 */
public record WillOfTheCouncilEffect() implements CardEffect {
}
