package com.github.laxika.magicalvibes.model.effect;

/**
 * "That player exiles all cards from their library" — the controller of the resolving ability
 * exiles their whole library (Thought Lash's unpaid-cumulative-upkeep trigger).
 */
public record ExileControllerLibraryEffect() implements CardEffect {
}
