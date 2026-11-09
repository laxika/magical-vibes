package com.github.laxika.magicalvibes.model.effect;

/** Puts the top cards of the combat-damaged player's library onto the battlefield as Cybermen. */
public record PutTopCardsOfDamagedPlayerLibraryFaceDownAsCybermenEffect(int count)
        implements CardEffect {

    public PutTopCardsOfDamagedPlayerLibraryFaceDownAsCybermenEffect {
        if (count < 0) {
            throw new IllegalArgumentException("count cannot be negative");
        }
    }
}
