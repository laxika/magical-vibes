package com.github.laxika.magicalvibes.model.effect;

/**
 * Turn-scoped replacement effect that shuffles creatures entering from exile, including creatures
 * that were cast from exile, into their owners' libraries instead.
 */
public record ShuffleCreaturesEnteringFromExileEffect() implements CardEffect {
}
