package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a one-shot trigger for the activating player's next upkeep. When that trigger resolves,
 * it prompts for a basic land type and grants the source permanent the matching landwalk until end
 * of that turn.
 */
public record RegisterChosenLandwalkAtNextUpkeepEffect() implements CardEffect {
}
