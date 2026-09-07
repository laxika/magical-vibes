package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, registers a delayed trigger that creates the configured token(s) at the
 * beginning of the resolving controller's next upkeep.
 */
public record RegisterDelayedCreateTokenAtNextUpkeepEffect(CreateTokenEffect tokenEffect)
        implements CardEffect {
}
