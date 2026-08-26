package com.github.laxika.magicalvibes.model.effect;

/**
 * Static rules restriction: during the source controller's turn, permanents controlled by that
 * player's opponents can't be turned face up.
 */
public record OpponentsPermanentsCantBeTurnedFaceUpEffect() implements CardEffect {
}
