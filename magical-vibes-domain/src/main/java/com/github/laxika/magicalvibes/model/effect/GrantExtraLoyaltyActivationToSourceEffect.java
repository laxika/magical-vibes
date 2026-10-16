package com.github.laxika.magicalvibes.model.effect;

/**
 * Gives the source planeswalker one additional loyalty activation for the rest of the turn.
 *
 * @param nonStacking whether the grant only raises the limit to twice this turn ("twice this turn rather
 *                    than only once", Kaito, Dancing Shadow) instead of adding another activation each time
 *                    it resolves (Comet, Stellar Pup)
 */
public record GrantExtraLoyaltyActivationToSourceEffect(boolean nonStacking) implements CardEffect {

    public GrantExtraLoyaltyActivationToSourceEffect() {
        this(false);
    }
}
