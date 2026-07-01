package com.github.laxika.magicalvibes.model.effect;

/**
 * Discards the controller's entire hand, then draws cards equal to the number of cards
 * in target player's hand (counted at draw time).
 */
public record DiscardOwnHandThenDrawEqualToTargetPlayerHandSizeEffect() implements CardEffect {
    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
