package com.github.laxika.magicalvibes.model.effect;

/** Pregame marker for a card that may begin the game in its owner's graveyard. */
public record StartInGraveyardEffect(int lifeLoss) implements PregameGraveyardChoiceEffect {
}
