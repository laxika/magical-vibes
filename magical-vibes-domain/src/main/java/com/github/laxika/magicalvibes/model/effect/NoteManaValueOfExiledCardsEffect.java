package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for a permanent that notes the mana value of every card as it enters exile.
 * The note is held by the permanent for the current turn.
 */
public record NoteManaValueOfExiledCardsEffect() implements CardEffect {

    @Override
    public boolean notesManaValueOfExiledCards() {
        return true;
    }
}
