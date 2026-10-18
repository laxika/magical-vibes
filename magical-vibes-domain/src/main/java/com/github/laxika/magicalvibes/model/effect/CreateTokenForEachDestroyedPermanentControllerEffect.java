package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates one token under the controller of each permanent actually destroyed earlier on the
 * same stack entry. The destroy effect records one controller id per permanent, so duplicate ids
 * create multiple tokens for that player.
 *
 * @param nontokenOnly when true, only nontoken permanents from the destruction event contribute
 *                     tokens
 * @param eachTarget   when true, every permanent the destroy instruction applied to contributes a
 *                     token, even one that was not destroyed ("for each of those permanents")
 */
public record CreateTokenForEachDestroyedPermanentControllerEffect(
        CreateTokenEffect tokenEffect,
        boolean nontokenOnly,
        boolean eachTarget
) implements CardEffect {

    public CreateTokenForEachDestroyedPermanentControllerEffect(CreateTokenEffect tokenEffect, boolean nontokenOnly) {
        this(tokenEffect, nontokenOnly, false);
    }

    public CreateTokenForEachDestroyedPermanentControllerEffect(CreateTokenEffect tokenEffect) {
        this(tokenEffect, false, false);
    }

    /** One token per permanent the destroy instruction applied to, regardless of whether it was destroyed. */
    public static CreateTokenForEachDestroyedPermanentControllerEffect forEachTarget(CreateTokenEffect tokenEffect) {
        return new CreateTokenForEachDestroyedPermanentControllerEffect(tokenEffect, false, true);
    }
}
