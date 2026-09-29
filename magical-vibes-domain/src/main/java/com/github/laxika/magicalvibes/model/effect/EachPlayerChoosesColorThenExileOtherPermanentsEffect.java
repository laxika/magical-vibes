package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player chooses a color, then exiles every colored permanent except permanents that are
 * only the color chosen by their controller.
 */
public record EachPlayerChoosesColorThenExileOtherPermanentsEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
