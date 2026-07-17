package com.github.laxika.magicalvibes.model.effect;

/**
 * "Exile up to {@code maxCount} target attacking creatures." Modeled as a resolution-time
 * multi-select over every attacking creature on the battlefield (the controller may choose
 * zero) rather than a targeted spell, so it can ride on non-targeting resolution paths such
 * as a cycling trigger (Resounding Silence). Resolved by
 * {@code ExileUpToNAttackingCreaturesEffectHandler}, completed via
 * {@code MultiPermanentChoiceContext.ExileAttackingCreatures}.
 */
public record ExileUpToNAttackingCreaturesEffect(int maxCount) implements CardEffect {
}
