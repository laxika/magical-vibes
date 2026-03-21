package com.github.laxika.magicalvibes.model.effect;

/**
 * Triggered effect: "When this permanent leaves the battlefield, you lose the game."
 * Used by cards like Lich's Mastery. Fires on any battlefield departure (destruction,
 * exile, bounce, sacrifice, tuck).
 */
public record ControllerLosesGameOnLeavesEffect() implements CardEffect {
}
