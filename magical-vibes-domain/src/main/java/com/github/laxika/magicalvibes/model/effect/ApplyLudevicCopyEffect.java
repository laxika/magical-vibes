package com.github.laxika.magicalvibes.model.effect;

/**
 * Applies Ludevic's copy effect as part of transforming into Olag. The transform support handles
 * this marker before ordinary transform triggers are put on the stack.
 */
public record ApplyLudevicCopyEffect() implements CardEffect {
}
