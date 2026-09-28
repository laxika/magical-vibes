package com.github.laxika.magicalvibes.model.effect;

/** Static replacement that replaces the first eligible draw each turn with four cards. */
public record FirstNonDrawStepDrawFourReplacementEffect()
        implements FirstNonDrawStepDrawReplacementEffect {

    @Override
    public int replacementDrawCount() {
        return 4;
    }
}
