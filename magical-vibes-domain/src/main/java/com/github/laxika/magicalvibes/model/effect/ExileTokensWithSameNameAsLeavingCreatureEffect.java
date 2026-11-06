package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles every token on the battlefield with the name of the nontoken creature that caused the
 * source's leave trigger: the stack entry's triggering permanent, as it last existed on the battlefield (CR 603.10a).
 */
public record ExileTokensWithSameNameAsLeavingCreatureEffect() implements CardEffect {
}
