package com.github.laxika.magicalvibes.model.effect;

/** Makes each opponent with no cards in hand lose a fixed amount of life. */
public record EachOpponentWithEmptyHandLosesLifeEffect(int amount) implements CardEffect {
}
