package com.github.laxika.magicalvibes.model.effect;

/**
 * "As this permanent transforms into [this face], [effect]": a static ability of the face it transforms into, applied
 * while the permanent transforms, without using the stack. Its effect is an emblem (Sephiroth, One-Winged Angel) or
 * Olag's copy of a creature card exiled with Ludevic ({@link ApplyLudevicCopyEffect}).
 *
 * @param effect what happens as it transforms
 */
public record AsTransformsIntoThisEffect(CardEffect effect) implements ReplacementEffect {
}
