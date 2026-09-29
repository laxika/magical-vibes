package com.github.laxika.magicalvibes.model.effect;

/**
 * Starting with the effect controller, each player votes for planeswalk or chaos. A planeswalk
 * majority planeswalks; otherwise, including a tie, chaos ensues.
 */
public record WillOfThePlaneswalkersEffect() implements CardEffect {
}
