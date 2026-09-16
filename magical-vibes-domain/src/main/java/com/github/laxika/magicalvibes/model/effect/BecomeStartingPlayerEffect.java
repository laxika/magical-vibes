package com.github.laxika.magicalvibes.model.effect;

/** Pregame choice for a card that can make its controller the starting player. */
public record BecomeStartingPlayerEffect() implements PregameChoiceEffect {

    @Override
    public boolean onlyForNonStartingPlayer() {
        return true;
    }
}
