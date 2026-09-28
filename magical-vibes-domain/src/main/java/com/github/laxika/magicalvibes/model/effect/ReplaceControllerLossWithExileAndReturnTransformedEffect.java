package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect for Liliana's Other Contract: if its controller would lose the
 * game, exile this permanent, then return it to the battlefield transformed under its
 * controller's control.
 */
public record ReplaceControllerLossWithExileAndReturnTransformedEffect() implements CardEffect {
}
