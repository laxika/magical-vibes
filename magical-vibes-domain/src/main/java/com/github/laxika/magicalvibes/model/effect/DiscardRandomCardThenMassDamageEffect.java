package com.github.laxika.magicalvibes.model.effect;

/**
 * Discards a card at random, then deals damage equal to that card's mana value to each creature.
 * When the discard succeeds, the handler offers to repeat the process.
 */
public record DiscardRandomCardThenMassDamageEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
