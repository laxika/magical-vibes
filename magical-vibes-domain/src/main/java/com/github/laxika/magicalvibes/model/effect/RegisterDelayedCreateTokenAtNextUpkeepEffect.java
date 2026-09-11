package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, registers a delayed trigger that creates the configured token(s) at the
 * beginning of the resolving controller's next upkeep.
 */
public record RegisterDelayedCreateTokenAtNextUpkeepEffect(CreateTokenEffect tokenEffect,
                                                           boolean anyPlayerNextUpkeep)
        implements CardEffect {

    public RegisterDelayedCreateTokenAtNextUpkeepEffect(CreateTokenEffect tokenEffect) {
        this(tokenEffect, false);
    }

    public static RegisterDelayedCreateTokenAtNextUpkeepEffect atAnyPlayerNextUpkeep(
            CreateTokenEffect tokenEffect) {
        return new RegisterDelayedCreateTokenAtNextUpkeepEffect(tokenEffect, true);
    }
}
