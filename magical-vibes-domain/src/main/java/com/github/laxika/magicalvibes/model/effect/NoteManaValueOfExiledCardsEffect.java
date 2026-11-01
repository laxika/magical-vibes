package com.github.laxika.magicalvibes.model.effect;

/**
 * Static ability: "Note the mana value of each card as it's put into exile" (Bell Borca, Spectral Sergeant).
 * {@code GameData} records the note on the permanent for the current turn as each card is exiled.
 */
public record NoteManaValueOfExiledCardsEffect() implements CardEffect {

    @Override
    public boolean notesManaValueOfExiledCards() {
        return true;
    }
}
