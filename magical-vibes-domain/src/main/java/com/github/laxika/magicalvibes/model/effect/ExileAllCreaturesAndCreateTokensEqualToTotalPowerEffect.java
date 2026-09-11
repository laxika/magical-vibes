package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles every creature, then has each creature's controller create a number of the supplied
 * token equal to the total power of that player's exiled creatures.
 */
public record ExileAllCreaturesAndCreateTokensEqualToTotalPowerEffect(CreateTokenEffect tokenTemplate)
        implements CardEffect {
}
