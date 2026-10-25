package com.github.laxika.magicalvibes.service.effect.normalfx;

/**
 * Stops a resolving effect handler marked
 * {@link com.github.laxika.magicalvibes.service.effect.ChoosesCoinFlipResultsFirst} at its first coin
 * flip, because the flipping player must choose which Krark's Thumb results to ignore.
 * {@code EffectResolutionService} catches it and resolves the effect again once answered.
 */
public final class CoinFlipChoicePending extends RuntimeException {

    public CoinFlipChoicePending() {
        super("A coin flip's kept result must be chosen before the effect resolves", null, false, false);
    }
}
