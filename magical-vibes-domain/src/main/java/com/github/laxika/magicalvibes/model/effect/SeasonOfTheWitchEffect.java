package com.github.laxika.magicalvibes.model.effect;

/** Destroys untapped creatures that could have attacked but did not attack this turn. */
public record SeasonOfTheWitchEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
