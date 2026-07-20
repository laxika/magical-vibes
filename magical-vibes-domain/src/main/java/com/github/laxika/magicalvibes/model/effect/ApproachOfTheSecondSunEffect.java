package com.github.laxika.magicalvibes.model.effect;

/**
 * Approach of the Second Sun's resolution: if this spell was cast from its controller's hand and they have
 * cast another spell with the same name this game, they win the game. Otherwise the spell is put into its
 * owner's library seventh from the top and its controller gains 7 life.
 */
public record ApproachOfTheSecondSunEffect() implements CardEffect {
}
