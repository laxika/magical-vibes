package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates one token under the controller of each permanent actually destroyed earlier on the
 * same stack entry. The destroy effect records one controller id per permanent, so duplicate ids
 * create multiple tokens for that player.
 *
 * @param nontokenOnly when true, only nontoken permanents from the destruction event contribute
 *                     tokens
 */
public record CreateTokenForEachDestroyedPermanentControllerEffect(
        CreateTokenEffect tokenEffect,
        boolean nontokenOnly
) implements CardEffect {

    public CreateTokenForEachDestroyedPermanentControllerEffect(CreateTokenEffect tokenEffect) {
        this(tokenEffect, false);
    }
}
