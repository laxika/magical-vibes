package com.github.laxika.magicalvibes.model.effect;

/**
 * "At the beginning of each end step, each player sacrifices a permanent of their choice unless
 * they discard a card," or the same choice repeated for each card drawn by the controller during
 * the current stack-entry resolution.
 */
public record EachPlayerSacrificesPermanentUnlessDiscardEffect(boolean repeatForEachCardDrawn)
        implements CardEffect {

    public EachPlayerSacrificesPermanentUnlessDiscardEffect() {
        this(false);
    }

    public static EachPlayerSacrificesPermanentUnlessDiscardEffect forEachCardDrawn() {
        return new EachPlayerSacrificesPermanentUnlessDiscardEffect(true);
    }
}
