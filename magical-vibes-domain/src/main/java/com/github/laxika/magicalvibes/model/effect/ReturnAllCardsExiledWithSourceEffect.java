package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns all cards exiled with the source permanent (tracked via
 * {@code GameData.exiledCards} by source permanent ID) to the battlefield. Used as a
 * death trigger by cards like Helvault whose abilities accumulate exiled cards and
 * release them when the source is put into a graveyard from the battlefield, and as a
 * sacrifice ability by Endless Sands and Cold Storage.
 *
 * @param underControllerControl when {@code true} the cards return under the ability
 *        controller's control ("under your control", Cold Storage); when {@code false}
 *        under their owners' control (Helvault, Endless Sands).
 */
public record ReturnAllCardsExiledWithSourceEffect(boolean underControllerControl) implements CardEffect {

    public ReturnAllCardsExiledWithSourceEffect() {
        this(false);
    }
}
