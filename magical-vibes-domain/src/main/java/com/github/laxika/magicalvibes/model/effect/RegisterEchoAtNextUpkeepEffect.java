package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers the source permanent's one-time echo payment trigger for its controller's next upkeep.
 */
public record RegisterEchoAtNextUpkeepEffect(String manaCost) implements CardEffect {
}
