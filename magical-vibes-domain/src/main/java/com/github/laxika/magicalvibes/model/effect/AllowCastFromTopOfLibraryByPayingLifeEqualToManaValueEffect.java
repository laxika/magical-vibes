package com.github.laxika.magicalvibes.model.effect;

/**
 * Static permission to cast nonland spells from the top of the controller's library by paying
 * life equal to the spell's mana value instead of paying its mana cost.
 */
public record AllowCastFromTopOfLibraryByPayingLifeEqualToManaValueEffect() implements CardEffect {
}
