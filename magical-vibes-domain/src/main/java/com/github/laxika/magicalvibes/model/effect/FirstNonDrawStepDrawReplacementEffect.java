package com.github.laxika.magicalvibes.model.effect;

/** Capability for a replacement that applies once each turn to the first draw outside a draw step's first draw. */
public interface FirstNonDrawStepDrawReplacementEffect extends CardEffect {

    int replacementDrawCount();
}
